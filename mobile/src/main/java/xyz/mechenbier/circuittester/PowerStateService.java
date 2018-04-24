package xyz.mechenbier.circuittester;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PowerStateService extends IntentService {

  private PowerConnectionReceiver pConRec = new PowerConnectionReceiver();

  // TODO: Rename actions, choose action names that describe tasks that this
  // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
  private static final String ACTION_RESUME = "xyz.mechenbier.circuittester.action.RESUME";
  private static final String ACTION_PAUSE = "xyz.mechenbier.circuittester.action.PAUSE";
  private static final String ACTION_MUTE = "xyz.mechenbier.circuittester.action.MUTE";

  public PowerStateService() {
    super("PowerStateService");
  }

  /**
   * Starts this service to perform action Foo with the given parameters. If
   * the service is already performing a task this action will be queued.
   *
   * @see IntentService
   */
  // TODO: Customize helper method
  public static void startActionFoo(Context context) {
    Intent intent = new Intent(context, PowerStateService.class);
    intent.setAction(ACTION_RESUME);
    context.startService(intent);
  }

  /**
   * Starts this service to perform action Baz with the given parameters. If
   * the service is already performing a task this action will be queued.
   *
   * @see IntentService
   */
  // TODO: Customize helper method
  public static void startActionBaz(Context context) {
    Intent intent = new Intent(context, PowerStateService.class);
    intent.setAction(ACTION_PAUSE);
    context.startService(intent);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_RESUME.equals(action)) {
        handleResume();
      } else if (ACTION_PAUSE.equals(action)) {
        handlePause();
      }
    }
  }

  private void handleResume() {
    // TODO: Handle action Foo
    throw new UnsupportedOperationException("Not yet implemented");
  }

  private void handlePause() {
    // TODO: Handle action Baz
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
