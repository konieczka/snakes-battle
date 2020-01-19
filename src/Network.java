import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

// This class is a convenient place to keep things common to both the client and server.
public class Network {
    static public final int portTCP = 5000;
    static public final int portUDP = 6000;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Login.class);
        kryo.register(RegistrationRequired.class);
        kryo.register(Register.class);
        kryo.register(AddSnake.class);
        kryo.register(UpdateSnake.class);
        kryo.register(RemoveSnake.class);
        kryo.register(Snake.class);
        kryo.register(MoveSnake.class);
    }

    static public class Login {
        public String name;
    }

    static public class RegistrationRequired {
    }

    static public class Register {
        public String name;
    }

    static public class UpdateSnake {
        public int id;
        public float x, y;
    }

    static public class AddSnake {
        public Snake character;
    }

    static public class RemoveSnake {
        public int id;
    }

    static public class MoveSnake {
        public float x, y;
    }
}