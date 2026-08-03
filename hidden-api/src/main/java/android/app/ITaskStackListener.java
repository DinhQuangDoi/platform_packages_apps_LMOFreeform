package android.app;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.window.TaskSnapshot;

/**
 * Hidden API {@code android.app.ITaskStackListener} (AIDL interface). Only the methods
 * actually implemented by {@code FreeformTaskStackListener} are stubbed; the runtime
 * class provides no-op defaults for everything else.
 */
public interface ITaskStackListener extends IInterface {
    void onTaskStackChanged();

    void onActivityPinned(String packageName, int userId, int taskId, int stackId);

    void onActivityUnpinned();

    void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible,
                                  boolean clearedTask, boolean wasVisible);

    void onActivityForcedResizable(String packageName, int taskId, int reason);

    void onActivityDismissingDockedTask();

    void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo,
                                                  int requestedDisplayId);

    void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo,
                                                    int requestedDisplayId);

    void onTaskCreated(int taskId, ComponentName componentName);

    void onTaskRemoved(int taskId);

    void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo);

    void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo);

    void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation);

    void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo);

    void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo, int userId);

    void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot);

    void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo);

    void onTaskDisplayChanged(int taskId, int newDisplayId);

    void onRecentTaskListUpdated();

    void onRecentTaskListFrozenChanged(boolean frozen);

    void onRecentTaskRemovedForAddTask(int taskId);

    void onTaskFocusChanged(int taskId, boolean focused);

    void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation);

    void onActivityRotation(int displayId);

    void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo);

    void onLockTaskModeChanged(int mode);

    void onTaskSnapshotInvalidated(int taskId);

    abstract class Stub extends Binder implements ITaskStackListener {
        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
