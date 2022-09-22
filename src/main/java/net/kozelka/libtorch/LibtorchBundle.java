package net.kozelka.libtorch;

import com.facebook.soloader.nativeloader.NativeLoader;
import com.facebook.soloader.nativeloader.SystemDelegate;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
}
