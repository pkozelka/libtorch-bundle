import java.io.File;
import java.util.Arrays;
import net.kozelka.libtorch.LibtorchBundle;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

/**
 * This class is meant to be used without any frameworks, completely standalone, for debugging with the lowest possible noise around.
 * <p>
 * Usage: java -cp libtorch-bundle-test.jar:libtorch-bundle.jar:linux_64.jar MinimalTest test/data/demo-model.pt1
 */
public class MinimalTest {
    static {
        // This is critically important
        LibtorchBundle.init();
    }

    public static void main(String[] args) {
        final File file = new File("src/test/data/demo-model.pt1").getAbsoluteFile();
        System.out.println("file = " + file);
        final Module mod = Module.load(file.getPath());
        final Tensor data =
            Tensor.fromBlob(
                new int[] {1, 2, 3, 4, 5, 6}, // data
                new long[] {2, 3} // shape
            );
        final IValue result = mod.forward(IValue.from(data), IValue.from(3.0));
        final Tensor output = result.toTensor();
        System.out.println("shape: " + Arrays.toString(output.shape()));
        System.out.println("data: " + Arrays.toString(output.getDataAsFloatArray()));
    }
}
