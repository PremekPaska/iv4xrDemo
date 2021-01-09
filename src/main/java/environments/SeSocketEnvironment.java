package environments;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.SeRequest;
import world.Observation;

import java.io.IOException;
import java.lang.reflect.Modifier;

public class SeSocketEnvironment extends Environment {

    public static final int DEFAULT_PORT = 9678;

    public SeSocketEnvironment(String host, int port) {
        socket = new SeSocketReaderWriter(host, port);
    }

    public Object sendCommand(EnvOperation cmd) {
        return sendCommand_(cmd);
    }

    private final SeSocketReaderWriter socket;

    // transient modifiers should be excluded, otherwise they will be send with json
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * This method provides a higher level wrapper over Environment.sendCommand. It
     * calls Environment.sendCommand which in turn will call SocketEnvironment.sendCommand_
     * It will also cast the json back to type T.
     * @param req
     * @param <T> any response type, make sure Unity actually sends this object back
     * @return response
     */
    public <T> T getSeResponse(SeRequest<T> req) {
        // WP note:
        // the actual id of the agent and the id of its target (if it interacts with
        // something) are put inside the req object ... :|
        return (T) sendCommand("APlib", "Se", "request", gson.toJson(req));
        // we do not have to cast to T, since req.responseType is of type Class<T>
        //System.out.println(json);
        return gson.fromJson(json, req.responseType);
    }

    /**
     * @param cmd representing the command to send to the real environment.
     * @return an object that the real environment sends back as the result of the
     * command, if any.
     */
    @Override
    protected Object sendCommand_(EnvOperation cmd) {

        try {
            Observation obs = sendPackage(cmd.arg) ;
            return Observation.toWorldModel(obs) ;
        }
        catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Primitive for sending a command-package to the game, and to return its response.
     * The command to send should be wrapped as a "Request" object.
     */
    private <T> T sendPackage(SeRequest<T> packageToSend) throws IOException {
        socket.write(packageToSend);
        return socket.read(packageToSend.responseType);
    }
}
