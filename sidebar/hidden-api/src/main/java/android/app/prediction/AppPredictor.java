package android.app.prediction;

import java.util.List;
import java.util.concurrent.Executor;

public class AppPredictor {
    public interface Callback {
        void onTargetsAvailable(List<AppTarget> targets);
    }

    public void registerPredictionUpdates(Executor executor, Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public void unregisterPredictionUpdates(Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public void requestPredictionUpdate() {
        throw new RuntimeException("Stub!");
    }
}
