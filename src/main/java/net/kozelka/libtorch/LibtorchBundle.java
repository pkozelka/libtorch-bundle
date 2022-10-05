package net.kozelka.libtorch;

import com.facebook.soloader.nativeloader.NativeLoader;
import com.facebook.soloader.nativeloader.SystemDelegate;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

public class LibtorchBundle {
    static {
        init();
    }
    public static void init() {
        NativeLoader.initIfUninitialized(new MySystemDelegate());
    }
    static class MySystemDelegate extends SystemDelegate {
        private final Set<String> alreadyLoaded = new HashSet<>();
        @Override
        public boolean loadLibrary(String shortName, int flags) {
            if (alreadyLoaded.contains(shortName)) {
                return false;
            }
            try {
                org.scijava.nativelib.NativeLoader.loadLibrary(shortName);
                // Problem 1: scijava only extracts the target SO but not its dependencies
                // Problem 2: even if I do that manually, the call crashes (coredumps)
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            alreadyLoaded.add(shortName);
            return true;
        }
    }

    /**
     * This entry-point only serves for functionality testing, both during build and on the deployment.
     * It is not meant as a regular CLI.
     * @param args -
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Expected exactly 1 argument");
        }
        final File file = new File(args[0]).getAbsoluteFile();
        System.err.println("Model file: " + file);
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
