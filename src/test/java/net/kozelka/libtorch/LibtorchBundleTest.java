package net.kozelka.libtorch;

import java.io.File;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

/**
 * NOTE: in order for this test to work in IDE, you must manually add directory `target/bundles` into the classpath (dependencies) !!!
 */
public class LibtorchBundleTest {
    static {
        // This is critically important
        LibtorchBundle.init();
    }

    @Test
    public void demoModel() {
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
