package xyz.mechenbier.circuittester;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PowerStateService extends Service {

  private PowerConnectionReceiver pConRec = new PowerConnectionReceiver();

  // TODO: Rename actions, choose action names that describe tasks that this
  // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
  private static final String ACTION_RESUME = "xyz.mechenbier.circuittester.action.RESUME";
  private static final String ACTION_PAUSE = "xyz.mechenbier.circuittester.action.PAUSE";
  private static final String ACTION_MUTE = "xyz.mechenbier.circuittester.action.MUTE";

  public PowerStateService() {
    super();
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
  public void onCreate() {
    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.  We also make it
    // background priority so CPU-intensive work will not disrupt our UI.
    pConRec.init(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
    pConRec.init(this);
    pConRec.Resume(false);
    return super.onStartCommand(intent, flags, startId);
  }

//  @Override
//  protected void onHandleIntent(Intent intent) {
//    if (intent != null) {
//      final String action = intent.getAction();
//      if (ACTION_RESUME.equals(action)) {
//        handleResume();
//      } else if (ACTION_PAUSE.equals(action)) {
//        handlePause();
//      }
//    }
//  }

  private void handleResume() {
    // TODO: Handle action Foo
    throw new UnsupportedOperationException("Not yet implemented");
  }

  private void handlePause() {
    // TODO: Handle action Baz
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public IBinder onBind(Intent intent) {
    // We don't provide binding, so return null
    return null;
  }

  @Override
  public void onDestroy() {
    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
  }
}
