package com.ford.syncV4.android.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.ford.syncV4.android.MainApp;
import com.ford.syncV4.android.R;
import com.ford.syncV4.android.activity.SyncProxyTester;
import com.ford.syncV4.android.adapters.LogAdapter;
import com.ford.syncV4.android.constants.Const;
import com.ford.syncV4.android.listener.ConnectionListenersManager;
import com.ford.syncV4.android.manager.AppIdManager;
import com.ford.syncV4.android.manager.AppPreferencesManager;
import com.ford.syncV4.android.manager.ApplicationIconManager;
import com.ford.syncV4.android.manager.LastUsedHashIdsManager;
import com.ford.syncV4.android.manager.PutFileTransferManager;
import com.ford.syncV4.android.manager.RPCRequestsResumableManager;
import com.ford.syncV4.android.module.ModuleTest;
import com.ford.syncV4.android.policies.PoliciesTest;
import com.ford.syncV4.android.policies.PoliciesTesterActivity;
import com.ford.syncV4.android.policies.PolicyFilesManager;
import com.ford.syncV4.android.receivers.SyncReceiver;
import com.ford.syncV4.android.service.proxy.OnSystemRequestHandler;
import com.ford.syncV4.android.utils.AppUtils;
import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.marshal.IJsonRPCMarshaller;
import com.ford.syncV4.protocol.enums.ServiceType;
import com.ford.syncV4.proxy.RPCRequest;
import com.ford.syncV4.proxy.RPCRequestFactory;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.SyncProxyConfigurationResources;
import com.ford.syncV4.proxy.constants.Names;
import com.ford.syncV4.proxy.constants.ProtocolConstants;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALMTesting;
import com.ford.syncV4.proxy.rpc.AddCommand;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenu;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AlertManeuverResponse;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.Choice;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSet;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.DiagnosticMessageResponse;
import com.ford.syncV4.proxy.rpc.EncodedSyncPDataResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GenericResponse;
import com.ford.syncV4.proxy.rpc.GetDTCsResponse;
import com.ford.syncV4.proxy.rpc.GetVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.ListFilesResponse;
import com.ford.syncV4.proxy.rpc.OnAudioPassThru;
import com.ford.syncV4.proxy.rpc.OnButtonEvent;
import com.ford.syncV4.proxy.rpc.OnButtonPress;
import com.ford.syncV4.proxy.rpc.OnCommand;
import com.ford.syncV4.proxy.rpc.OnDriverDistraction;
import com.ford.syncV4.proxy.rpc.OnEncodedSyncPData;
import com.ford.syncV4.proxy.rpc.OnHMIStatus;
import com.ford.syncV4.proxy.rpc.OnHashChange;
import com.ford.syncV4.proxy.rpc.OnKeyboardInput;
import com.ford.syncV4.proxy.rpc.OnLanguageChange;
import com.ford.syncV4.proxy.rpc.OnPermissionsChange;
import com.ford.syncV4.proxy.rpc.OnSyncPData;
import com.ford.syncV4.proxy.rpc.OnSystemRequest;
import com.ford.syncV4.proxy.rpc.OnTBTClientState;
import com.ford.syncV4.proxy.rpc.OnTouchEvent;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.PerformAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.PerformInteractionResponse;
import com.ford.syncV4.proxy.rpc.PutFile;
import com.ford.syncV4.proxy.rpc.PutFileResponse;
import com.ford.syncV4.proxy.rpc.ReadDIDResponse;
import com.ford.syncV4.proxy.rpc.RegisterAppInterface;
import com.ford.syncV4.proxy.rpc.RegisterAppInterfaceResponse;
import com.ford.syncV4.proxy.rpc.ResetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.ScrollableMessageResponse;
import com.ford.syncV4.proxy.rpc.SetAppIcon;
import com.ford.syncV4.proxy.rpc.SetAppIconResponse;
import com.ford.syncV4.proxy.rpc.SetDisplayLayoutResponse;
import com.ford.syncV4.proxy.rpc.SetGlobalProperties;
import com.ford.syncV4.proxy.rpc.SetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.SetMediaClockTimerResponse;
import com.ford.syncV4.proxy.rpc.Show;
import com.ford.syncV4.proxy.rpc.ShowConstantTBTResponse;
import com.ford.syncV4.proxy.rpc.ShowResponse;
import com.ford.syncV4.proxy.rpc.SliderResponse;
import com.ford.syncV4.proxy.rpc.SpeakResponse;
import com.ford.syncV4.proxy.rpc.SubscribeButton;
import com.ford.syncV4.proxy.rpc.SubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleData;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.SyncMsgVersion;
import com.ford.syncV4.proxy.rpc.SyncPDataResponse;
import com.ford.syncV4.proxy.rpc.SystemRequestResponse;
import com.ford.syncV4.proxy.rpc.UnregisterAppInterfaceResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleData;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.UpdateTurnListResponse;
import com.ford.syncV4.proxy.rpc.enums.AppHMIType;
import com.ford.syncV4.proxy.rpc.enums.AppInterfaceUnregisteredReason;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.FileType;
import com.ford.syncV4.proxy.rpc.enums.HMILevel;
import com.ford.syncV4.proxy.rpc.enums.Language;
import com.ford.syncV4.proxy.rpc.enums.RequestType;
import com.ford.syncV4.proxy.rpc.enums.Result;
import com.ford.syncV4.session.Session;
import com.ford.syncV4.test.ITestConfigCallback;
import com.ford.syncV4.test.TestConfig;
import com.ford.syncV4.transport.BTTransportConfig;
import com.ford.syncV4.transport.BaseTransportConfig;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.transport.TransportType;
import com.ford.syncV4.transport.usb.USBTransportConfig;
import com.ford.syncV4.util.Base64;
import com.ford.syncV4.util.logger.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyService extends Service implements IProxyListenerALMTesting, ITestConfigCallback {

    static final String TAG = ProxyService.class.getSimpleName();

    public static final int HEARTBEAT_INTERVAL = 5000;
    public static final int HEARTBEAT_INTERVAL_MAX = Integer.MAX_VALUE;
    private Integer autoIncCorrId = 1;

    private static final String ICON_SYNC_FILENAME = "icon.png";
    private static final String ICON_FILENAME_SUFFIX = ".png";

    private static final int XML_TEST_COMMAND = 100;
    private static final int POLICIES_TEST_COMMAND = 101;

    private SyncProxyALM mSyncProxy;
    private final Vector<LogAdapter> mLogAdapters = new Vector<LogAdapter>();
    private ModuleTest mTesterMain;
    private MediaPlayer mEmbeddedAudioPlayer;
    private Boolean playingAudio = false;
    protected SyncReceiver mediaButtonReceiver;

    private boolean firstHMIStatusChange = true;
    private HMILevel prevHMILevel = HMILevel.HMI_NONE;

    private boolean mWaitingForResponse = false;
    private IProxyServiceEvent mProxyServiceEvent;
    private ICloseSession mCloseSessionCallback;

    private PutFileTransferManager mPutFileTransferManager;
    private ConnectionListenersManager mConnectionListenersManager;
    private final IBinder mBinder = new ProxyServiceBinder(this);
    // This manager provide functionality to process RPC requests which are involved in app resumption
    private RPCRequestsResumableManager mRpcRequestsResumableManager =
            new RPCRequestsResumableManager();

    /**
     * Map of the existed syncProxyTester applications, mobile application Id is a Key
     */
    private final HashMap<String, RegisterAppInterface> registerAppInterfaceHashMap =
            new HashMap<String, RegisterAppInterface>();

    /**
     * Temporary Map fo the AppId and SessionId
     */
    private final HashMap<String, Byte> appIdToSessionIdMap = new HashMap<String, Byte>();

    private String mActiveAppId = "";

    // This Config object stores all the necessary data for SDK testing
    private TestConfig mTestConfig = new TestConfig();

    private final AtomicInteger mSessionsCounter = new AtomicInteger(0);

    @Override
    public void onCreate() {
        super.onCreate();

        createInfoMessageForAdapter("ProxyService.onCreate()");
        Logger.i(TAG + " OnCreate, mSyncProxy:" + mSyncProxy);

        // Init Listener managers (ConnectionListenersManager, etc ...)
        mConnectionListenersManager = new ConnectionListenersManager();

        IntentFilter mediaIntentFilter = new IntentFilter();
        mediaIntentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);

        mediaButtonReceiver = new SyncReceiver();
        registerReceiver(mediaButtonReceiver, mediaIntentFilter);

        //startProxyIfNetworkConnected();

        mPutFileTransferManager = new PutFileTransferManager();

        mRpcRequestsResumableManager.setCallback(new RPCRequestsResumableManager.RPCRequestsResumableManagerCallback() {
            @Override
            public void onSendRequest(RPCRequest request) {
                syncProxySendRPCRequestWithPreprocess(request);
            }
        });

        MainApp.getInstance().getLastUsedHashIdsManager().init();
    }

    public void showLockMain() {
        if (SyncProxyTester.getInstance() == null) {
            Intent i = new Intent(this, SyncProxyTester.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            // quite a few things downstream depend on the main activity and its
            // fields being alive, so wait for a while here
            int numTries = 9;
            while ((SyncProxyTester.getInstance() == null) && (numTries-- >= 0)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Logger.d(TAG, "created " + SyncProxyTester.getInstance());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG + " OnStartCommand");
        createInfoMessageForAdapter("ProxyService.onStartCommand()");
        return START_STICKY;
    }

    /**
     * Function checks if WiFi enabled.
     * Manifest permission is required:
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     *
     * @return true if enabled
     */
    private boolean hasWiFiConnection() {
        boolean result = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            if (netInfo != null) {
                for (NetworkInfo ni : netInfo) {
                    if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                        Logger.d(TAG, ni.getTypeName());
                        if (ni.isConnected()) {
                            Logger.d(TAG,
                                    "ProxyService().hasWiFiConnection(): wifi conncetion found");
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    public void startProxyIfNetworkConnected() {
        SharedPreferences prefs = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        int transportType = prefs.getInt(
                Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_TYPE);
        Logger.i(TAG, "ProxyService. Start Proxy If Network Connected");
        boolean doStartProxy = false;
        if (transportType == Const.Transport.KEY_BLUETOOTH) {
            Logger.i(TAG, "ProxyService. Transport = Bluetooth.");
            BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter != null) {
                if (mBtAdapter.isEnabled()) {
                    doStartProxy = true;
                }
            }
        } else {
            Logger.i(TAG, "ProxyService. Transport = Default.");
            //TODO: This code is commented out for simulator purposes
            /*
            Logger.d(CLASS_NAME, "ProxyService. onStartCommand(). Transport = WiFi.");
			if (hasWiFiConnection() == true) {
				Logger.d(CLASS_NAME, "ProxyService. onStartCommand(). WiFi enabled.");
				startProxy();
			} else {
				Logger.w(CLASS_NAME,
						"ProxyService. onStartCommand(). WiFi is not enabled.");
			}
			*/
            doStartProxy = true;
        }
        if (doStartProxy) {

            // Prepare all necessary data that need to be use in the Tests
            prepareTestConfig();

            boolean result = startProxy();
            Logger.i(TAG + " Proxy complete result:" + result);
        }
    }

    public String getActiveAppId() {
        return mActiveAppId;
    }

    public void setActiveAppId(String value) {
        Logger.d(TAG + " set AppId:" + value);
        mActiveAppId = value;
        if (mSyncProxy != null) {
            mSyncProxy.setActiveAppId(value);
        }
    }

    /**
     * @return an object which contains Testing configuration data
     */
    public TestConfig getTestConfig() {
        return mTestConfig;
    }

    /**
     * Prepare all necessary parameters to be passed to Sync proxy
     */
    private void prepareTestConfig() {
        mTestConfig.setUseHashId(AppPreferencesManager.getUseHashId());
        mTestConfig.setCustomHashId(AppPreferencesManager.getCustomHashId());
        mTestConfig.setUseCustomHashId(AppPreferencesManager.getUseCustomHashId());
    }

    private boolean startProxy() {
        createInfoMessageForAdapter("ProxyService.startProxy()");
        Logger.i(TAG + " Start Proxy");

        if (mSyncProxy == null) {
            try {
                SharedPreferences settings = getSharedPreferences(Const.PREFS_NAME, 0);
                boolean isMediaApp = settings.getBoolean(
                        Const.PREFS_KEY_ISMEDIAAPP,
                        Const.PREFS_DEFAULT_ISMEDIAAPP);
                boolean isNaviApp = settings.getBoolean(
                        Const.PREFS_KEY_ISNAVIAPP,
                        Const.PREFS_DEFAULT_ISNAVIAPP);
                int versionNumber = getCurrentProtocolVersion();
                String appName = settings.getString(Const.PREFS_KEY_APPNAME,
                        Const.PREFS_DEFAULT_APPNAME);
                Language lang = Language.valueOf(settings.getString(
                        Const.PREFS_KEY_LANG, Const.PREFS_DEFAULT_LANG));
                Language hmiLang = Language.valueOf(settings.getString(
                        Const.PREFS_KEY_HMILANG, Const.PREFS_DEFAULT_HMILANG));
                Logger.i(TAG, " Using protocol version " + versionNumber);
                String ipAddress = settings.getString(
                        Const.Transport.PREFS_KEY_TRANSPORT_IP,
                        Const.Transport.PREFS_DEFAULT_TRANSPORT_IP);
                int tcpPort = settings.getInt(
                        Const.Transport.PREFS_KEY_TRANSPORT_PORT,
                        Const.Transport.PREFS_DEFAULT_TRANSPORT_PORT);
                boolean mIsNSD = settings.getBoolean(Const.Transport.PREFS_KEY_IS_NSD, false);

                SyncMsgVersion syncMsgVersion = new SyncMsgVersion();
                syncMsgVersion.setMajorVersion(2);
                syncMsgVersion.setMinorVersion(2);
                Vector<AppHMIType> appHMITypes = createAppTypeVector(isNaviApp);
                BaseTransportConfig transportConfig = null;
                TransportType transportType = AppPreferencesManager.getTransportType();
                String appID = AppIdManager.getAppIdByTransport(transportType);
                switch (transportType) {
                    case BLUETOOTH:
                        transportConfig = new BTTransportConfig();
                        break;
                    case TCP:
                        transportConfig = new TCPTransportConfig(tcpPort, ipAddress);
                        ((TCPTransportConfig) transportConfig).setIsNSD(mIsNSD);
                        ((TCPTransportConfig) transportConfig).setApplicationContext(this);
                        break;
                    case USB:
                        transportConfig = new USBTransportConfig(getApplicationContext());
                        break;
                }

                // Apply custom AppId in case of such possibility selected
                if (AppPreferencesManager.getIsCustomAppId()) {
                    appID = AppPreferencesManager.getCustomAppId();
                }

                SyncProxyConfigurationResources syncProxyConfigurationResources =
                        new SyncProxyConfigurationResources();
                syncProxyConfigurationResources.setTelephonyManager(
                        (TelephonyManager) MainApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE));

                mTestConfig.setProtocolMinVersion((byte) AppPreferencesManager.getProtocolMinVersion());
                mTestConfig.setProtocolMaxVersion((byte) AppPreferencesManager.getProtocolMaxVersion());

                mSyncProxy = new SyncProxyALM(this,
                        syncProxyConfigurationResources/*sync proxy configuration resources*/,
                        /*enable advanced lifecycle management true,*/
                        appName,
                        /*ngn media app*/null,
                        /*vr synonyms*/null,
                        /*is media app*/isMediaApp, appHMITypes,
                        syncMsgVersion,
                        /*language desired*/lang,
                        /*HMI Display Language Desired*/hmiLang,
                        appID,
                        /*autoActivateID*/null,
                        /*callbackToUIThre1ad*/ false,
                        /*preRegister*/ false,
                        versionNumber,
                        transportConfig, mTestConfig);
            } catch (SyncException e) {
                Logger.e(TAG, e.toString());
                //error creating proxy, returned proxy = null
                if (mSyncProxy == null) {
                    stopServiceBySelf();
                    return false;
                }
            }
        }

        // TODO : Add LogAdapters
        //OnSystemRequestHandler mOnSystemRequestHandler = new OnSystemRequestHandler(mLogAdapters);
        OnSystemRequestHandler mOnSystemRequestHandler = new OnSystemRequestHandler(null);

        mSyncProxy.setActiveAppId(mActiveAppId);
        mSyncProxy.setOnSystemRequestHandler(mOnSystemRequestHandler);
        mSyncProxy.setTestConfigCallback(this);

        createInfoMessageForAdapter("ProxyService.startProxy() complete");
        Logger.i(TAG + " Start Proxy complete:" + mSyncProxy);

        return mSyncProxy != null && mSyncProxy.getIsConnected();
    }

    private Vector<AppHMIType> createAppTypeVector(boolean naviApp) {
        if (naviApp) {
            Vector<AppHMIType> vector = new Vector<AppHMIType>();
            vector.add(AppHMIType.NAVIGATION);
            return vector;
        }
        return null;
    }

    private int getCurrentProtocolVersion() {
        return ProtocolConstants.PROTOCOL_VERSION_MIN;
    }

    @Override
    public void onDestroy() {
        createInfoMessageForAdapter("ProxyService.onDestroy()");

        // In case service is destroying by System
        if (mProxyServiceEvent == null) {
            // TODO : Reconsider this case, for instance if we just close Session
            //disposeSyncProxy();
        }
        setProxyServiceEvent(null);
        if (mEmbeddedAudioPlayer != null) {
            mEmbeddedAudioPlayer.release();
        }
        unregisterReceiver(mediaButtonReceiver);
        super.onDestroy();
    }

    public void sendPolicyTableUpdate(FileType fileType, RequestType requestType) {
        // TODO : Add LogAdapters
        //PolicyFilesManager.sendPolicyTableUpdate(mSyncProxy, fileType, requestType, mLogAdapters);
        PolicyFilesManager.sendPolicyTableUpdate(mSyncProxy, fileType, requestType, null);
    }

    public void setCloseSessionCallback(ICloseSession closeSessionCallback) {
        mCloseSessionCallback = closeSessionCallback;
    }

    public void setProxyServiceEvent(IProxyServiceEvent proxyServiceEvent) {
        mProxyServiceEvent = proxyServiceEvent;
    }

    public void destroyService() {
        disposeSyncProxy();
    }

    private void disposeSyncProxy() {
        createInfoMessageForAdapter("ProxyService.disposeSyncProxy()");

        MainApp.getInstance().getLastUsedHashIdsManager().save();

        if (mSyncProxy != null) {
            try {
                mSyncProxy.dispose();
            } catch (SyncException e) {
                Logger.e(TAG, e.toString());
                if (mProxyServiceEvent != null) {
                    mProxyServiceEvent.onDisposeError();
                }
            }
            mSyncProxy = null;
        }
    }

    /**
     * @return the number of the current session's services
     */
    public int getServicesNumber() {
        return mSyncProxy.getServicesNumber();
    }

    public boolean hasServiceInServicesPool(String appId, ServiceType serviceType) {
        return mSyncProxy != null && mSyncProxy.hasServiceInServicesPool(appId, serviceType);
    }

    private void initializePredefinedView() {
        Logger.d(TAG, "Initialize predefined view");
        playingAudio = true;
        playAnnoyingRepetitiveAudio();

        try {
            show("Sync Proxy", "Tester");
        } catch (SyncException e) {
            createErrorMessageForAdapter("Error sending show", e);
        }

        commandSubscribeButtonPredefined(ButtonName.OK, getNextCorrelationID());
        commandSubscribeButtonPredefined(ButtonName.SEEKLEFT, getNextCorrelationID());
        commandSubscribeButtonPredefined(ButtonName.SEEKRIGHT, getNextCorrelationID());
        commandSubscribeButtonPredefined(ButtonName.TUNEUP, getNextCorrelationID());
        commandSubscribeButtonPredefined(ButtonName.TUNEDOWN, getNextCorrelationID());

        Vector<ButtonName> buttons = new Vector<ButtonName>(Arrays.asList(new ButtonName[]{
                ButtonName.OK, ButtonName.SEEKLEFT, ButtonName.SEEKRIGHT, ButtonName.TUNEUP,
                ButtonName.TUNEDOWN}));
        SyncProxyTester.getInstance().buttonsSubscribed(buttons);

        commandAddCommandPredefined(XML_TEST_COMMAND, new Vector<String>(Arrays.asList(new String[]{"XML Test", "XML"})), "XML Test");
        commandAddCommandPredefined(POLICIES_TEST_COMMAND, new Vector<String>(Arrays.asList(new String[]{"Policies Test", "Policies"})), "Policies Test");
    }

    private void show(String mainField1, String mainField2) throws SyncException {
        Show msg = new Show();
        msg.setCorrelationID(getNextCorrelationID());
        msg.setMainField1(mainField1);
        msg.setMainField2(mainField2);
        createMessageForAdapter(msg, Log.DEBUG);
        mSyncProxy.sendRPCRequest(msg);
    }

    public void playPauseAnnoyingRepetitiveAudio() {
        if (mEmbeddedAudioPlayer != null && mEmbeddedAudioPlayer.isPlaying()) {
            playingAudio = false;
            pauseAnnoyingRepetitiveAudio();
        } else {
            playingAudio = true;
            playAnnoyingRepetitiveAudio();
        }
    }

    private void playAnnoyingRepetitiveAudio() {
        if (mEmbeddedAudioPlayer == null) {
            mEmbeddedAudioPlayer = MediaPlayer.create(this, R.raw.arco);
            mEmbeddedAudioPlayer.setLooping(true);
        }
        mEmbeddedAudioPlayer.start();

        createDebugMessageForAdapter("Playing audio");
    }

    public void pauseAnnoyingRepetitiveAudio() {
        if (mEmbeddedAudioPlayer != null && mEmbeddedAudioPlayer.isPlaying()) {
            mEmbeddedAudioPlayer.pause();

            createDebugMessageForAdapter("Paused Audio");
        }
    }

    public boolean isSyncProxyNotNull() {
        return mSyncProxy != null;
    }

    public boolean isSyncProxyConnected() {
        return mSyncProxy != null && mSyncProxy.getIsConnected();
    }

    public boolean isSyncProxyConnectionNotNull() {
        return mSyncProxy != null && mSyncProxy.getSyncConnection() != null;
    }

    public void startModuleTest() {
        // TODO : Add LogAdapters
        //mTesterMain = new ModuleTest(this, mLogAdapters);
        mTesterMain = new ModuleTest(this, null);
    }

    public void waiting(boolean waiting) {
        mWaitingForResponse = waiting;
    }

    /**
     * Add {@link com.ford.syncV4.android.adapters.LogAdapter} instance
     * @param logAdapter {@link com.ford.syncV4.android.adapters.LogAdapter}
     */
    public void addLogAdapter(LogAdapter logAdapter) {
        mLogAdapters.add(logAdapter);
    }

    public int getNextCorrelationID() {
        return autoIncCorrId++;
    }

    /**
     * Initialize new Session with RPC service only, this is a method for the Test Cases only,
     * for example: when {@link com.ford.syncV4.proxy.rpc.UnregisterAppInterface} is performed, for
     * the next Test it is necessary to restore RPC session
     */
    public void testInitializeSessionRPCOnly() {
        if (mSyncProxy == null) {
            Logger.e(TAG, "Sync Proxy is null when try to initialize test session");
            return;
        }

        TestConfig testConfig = mSyncProxy.getTestConfig();
        // It is important to set this value back to TRUE when concrete Test Case is complete.
        testConfig.setDoCallRegisterAppInterface(false);

        mSyncProxy.initializeSession();
    }

    @Override
    public void onOnHMIStatus(byte sessionId, OnHMIStatus notification) {
        //Logger.d(TAG + " OnHMIStatusChange AppId:" + mActiveAppId);

        createDebugMessageForAdapter(sessionId, notification);

        switch (notification.getSystemContext()) {
            case SYSCTXT_MAIN:
                break;
            case SYSCTXT_VRSESSION:
                break;
            case SYSCTXT_MENU:
                break;
            default:
                return;
        }

        switch (notification.getAudioStreamingState()) {
            case AUDIBLE:
                if (playingAudio) {
                    playAnnoyingRepetitiveAudio();
                }
                break;
            case NOT_AUDIBLE:
                pauseAnnoyingRepetitiveAudio();
                break;
            default:
                return;
        }

        final HMILevel curHMILevel = notification.getHmiLevel();
        final boolean appInterfaceRegistered = mSyncProxy.getAppInterfaceRegistered(mActiveAppId);

        if ((HMILevel.HMI_NONE == curHMILevel) && appInterfaceRegistered) {
            if (!isModuleTesting()) {
                if (AppPreferencesManager.getAutoSetAppIconFlag()) {
                    ApplicationIconManager.getInstance().setApplicationIcon(this);
                }
            }
        }

        if (prevHMILevel != curHMILevel) {
            boolean hmiChange = false;
            boolean hmiFull = false;
            switch (curHMILevel) {
                case HMI_FULL:
                    hmiFull = true;
                    hmiChange = true;
                    break;
                case HMI_LIMITED:
                    hmiChange = true;
                    break;
                case HMI_BACKGROUND:
                    hmiChange = true;
                    break;
                case HMI_NONE:
                    break;
                default:
                    return;
            }
            prevHMILevel = curHMILevel;

            if (appInterfaceRegistered) {
                if (hmiFull) {
                    if (firstHMIStatusChange) {
                        showLockMain();
                        // TODO : Add LogAdapters
                        //mTesterMain = new ModuleTest(this, mLogAdapters);
                        mTesterMain = new ModuleTest(this, null);
                        //mTesterMain = ModuleTest.getModuleTestInstance();

                        // Process an init state of the predefined requests here, assume that if
                        // hashId is not null means this is resumption
                        if (mSyncProxy.getHashId(mActiveAppId) == null) {
                            initializePredefinedView();
                        }
                    } else {
                        try {
                            if (mTesterMain != null && !mWaitingForResponse &&
                                    mTesterMain.getXMLTestThreadContext() != null) {
                                show("Sync Proxy", "Tester Ready");
                            }
                        } catch (SyncException e) {
                            createErrorMessageForAdapter(sessionId, "Error sending show");
                        }
                    }
                }

                if (hmiChange && firstHMIStatusChange) {
                    firstHMIStatusChange = false;

                    // Process an init state of the predefined requests here, assume that if
                    // hashId is not null means this is resumption
                    if (mSyncProxy.getHashId(mActiveAppId) == null) {
                        // upload turn icons
                        //Logger.d("Upload Icons");
                        sendIconFromResource(R.drawable.turn_left);
                        sendIconFromResource(R.drawable.turn_right);
                        sendIconFromResource(R.drawable.turn_forward);
                        sendIconFromResource(R.drawable.action);
                    }
                }
            }
        }
    }

    @Override
    public void onHashChange(byte sessionId, OnHashChange onHashChange) {
        createDebugMessageForAdapter(sessionId, onHashChange);

        LastUsedHashIdsManager lastUsedHashIdsManager =
                MainApp.getInstance().getLastUsedHashIdsManager();
        lastUsedHashIdsManager.addNewId(onHashChange.getHashID());
    }

    /**
     * Checks and returns if the module testing is in progress.
     *
     * @return true if the module testing is in progress
     */
    private boolean isModuleTesting() {
        return mWaitingForResponse && mTesterMain.getXMLTestThreadContext() != null;
    }

    public void sendIconFromResource(int resource) {
        commandPutFile(FileType.GRAPHIC_PNG,
                getResources().getResourceEntryName(resource) + ICON_FILENAME_SUFFIX,
                AppUtils.contentsOfResource(resource), getNextCorrelationID(), true);
    }

    @Override
    public void onOnCommand(byte sessionId, OnCommand notification) {
        createDebugMessageForAdapter(sessionId, notification);
        switch (notification.getCmdID()) {
            case XML_TEST_COMMAND:
                mTesterMain.restart(null);
                break;
            case POLICIES_TEST_COMMAND:
                // TODO : Add LogAdapters
                //PoliciesTest.runPoliciesTest(this, mLogAdapters);
                PoliciesTest.runPoliciesTest(this, null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProxyClosed(final String info, Exception e) {
        if (e != null) {
            createErrorMessageForAdapter("OnProxyClosed:" + info + ", msg:" + e.getMessage());
        } else {
            createErrorMessageForAdapter("OnProxyClosed:" + info);
        }
        boolean wasConnected = !firstHMIStatusChange;
        firstHMIStatusChange = true;
        prevHMILevel = HMILevel.HMI_NONE;
        ApplicationIconManager.getInstance().reset();

        if (wasConnected) {
            mConnectionListenersManager.dispatch();
        }

        if (!isModuleTesting()) {
            if (e == null) {
                return;
            }
            if (e instanceof SyncException) {
                final SyncExceptionCause cause = ((SyncException) e).getSyncExceptionCause();
                if ((cause != SyncExceptionCause.SYNC_PROXY_CYCLED) &&
                        (cause != SyncExceptionCause.BLUETOOTH_DISABLED) &&
                        (cause != SyncExceptionCause.SYNC_REGISTRATION_ERROR)) {
                    reset();
                }
            }
        }
    }

    public void reset() {
        if (mSyncProxy == null) {
            return;
        }
        // In case we run exit() - this is a quick marker of exiting.
        if (mProxyServiceEvent != null) {
            return;
        }
        try {
            mSyncProxy.resetProxy();
        } catch (SyncException e1) {
            Logger.e("Reset proxy error:" + e1);
            //something goes wrong, the proxy returns as null, stop the service.
            //do not want a running service with a null proxy
            if (mSyncProxy == null) {
                stopServiceBySelf();
            }
        }
    }

    /**
     * Restarting SyncProxyALM. For example after changing transport type
     */
    public void restart() {
        Logger.i(TAG, "ProxyService.Restart SyncProxyALM.");
        disposeSyncProxy();
        startProxyIfNetworkConnected();
    }

    @Override
    public void onError(String info, Throwable e) {
        createErrorMessageForAdapter("******onProxyError******", e);
        createErrorMessageForAdapter("Proxy error info: " + info);
    }

    /**
     * ******************************
     *  SYNC AppLink Base Callbacks *
     * ******************************
     */
    @Override
    public void onAddSubMenuResponse(final byte sessionId, AddSubMenuResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onAddSubMenuResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onCreateInteractionChoiceSetResponse(final byte sessionId,
                                                     CreateInteractionChoiceSetResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onCreateChoiceSetResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onDeleteCommandResponse(final byte sessionId, DeleteCommandResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onDeleteCommandResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(final byte sessionId,
                                                     DeleteInteractionChoiceSetResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onDeleteChoiceSetResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onDeleteSubMenuResponse(final byte sessionId, DeleteSubMenuResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onDeleteSubMenuResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onEncodedSyncPDataResponse(byte sessionId, EncodedSyncPDataResponse response) {
        Logger.i("syncp", "onEncodedSyncPDataResponse: " + response.getInfo() +
                response.getResultCode() + response.getSuccess());
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onResetGlobalPropertiesResponse(byte sessionId,
                                                ResetGlobalPropertiesResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSetMediaClockTimerResponse(byte sessionId, SetMediaClockTimerResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSpeakResponse(byte sessionId, SpeakResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSubscribeButtonResponse(byte sessionId, SubscribeButtonResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onUnsubscribeButtonResponse(byte sessionId, UnsubscribeButtonResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {
        createDebugMessageForAdapter(notification);
    }

    @Override
    public void onGenericResponse(GenericResponse response) {
        createDebugMessageForAdapter(response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(), response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    /**
     * *******************************************
     *  SYNC AppLink Soft Button Image Callbacks *
     * *******************************************
     */
    @Override
    public void onPutFileResponse(byte sessionId, PutFileResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final int receivedCorrelationId = response.getCorrelationID();

        if (AppPreferencesManager.getAutoSetAppIconFlag()) {
            ApplicationIconManager.getInstance().setAppIcon(this, receivedCorrelationId);
        }

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(receivedCorrelationId,
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
        mPutFileTransferManager.removePutFileFromAwaitArray(receivedCorrelationId);
    }

    @Override
    public void onDeleteFileResponse(byte sessionId, DeleteFileResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(), response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onListFilesResponse(byte sessionId, ListFilesResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(), response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSetAppIconResponse(byte sessionId, SetAppIconResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(), response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onOnButtonEvent(byte sessionId, OnButtonEvent notification) {
        createDebugMessageForAdapter(sessionId, notification);
    }

    @Override
    public void onOnButtonPress(byte sessionId, OnButtonPress notification) {
        createDebugMessageForAdapter(sessionId, notification);
        switch (notification.getButtonName()) {
            case OK:
                playPauseAnnoyingRepetitiveAudio();
                break;
            case SEEKLEFT:
                break;
            case SEEKRIGHT:
                break;
            case TUNEUP:
                break;
            case TUNEDOWN:
                break;
            default:
                break;
        }
    }

    /**
     * *********************************
     *  SYNC AppLink Updated Callbacks *
     * *********************************
     */
    @Override
    public void onAddCommandResponse(final byte sessionId, AddCommandResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final boolean success = response.getSuccess();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onAddCommandResponse(sessionId, success);
            }
        });

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onAlertResponse(byte sessionId, AlertResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onPerformInteractionResponse(byte sessionId, PerformInteractionResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSetGlobalPropertiesResponse(byte sessionId, SetGlobalPropertiesResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onShowResponse(byte sessionId, ShowResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    /**
     * *****************************
     *  SYNC AppLink New Callbacks *
     * *****************************
     */
    @Override
    public void onSliderResponse(byte sessionId, SliderResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onScrollableMessageResponse(byte sessionId, ScrollableMessageResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onChangeRegistrationResponse(byte sessionId, ChangeRegistrationResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSetDisplayLayoutResponse(byte sessionId, SetDisplayLayoutResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onOnLanguageChange(byte sessionId, OnLanguageChange notification) {
        createDebugMessageForAdapter(sessionId, notification);
    }

    @Override
    public void onPutFileRequest(byte sessionId, PutFile putFile) {
        createDebugMessageForAdapter(sessionId, putFile);
    }

    /**
     * *****************************************
     *  SYNC AppLink Audio Pass Thru Callbacks *
     * *****************************************
     */
    @Override
    public void onPerformAudioPassThruResponse(byte sessionId, PerformAudioPassThruResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }

        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final Result result = response.getResultCode();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onPerformAudioPassThruResponse(result);
            }
        });
    }

    @Override
    public void onEndAudioPassThruResponse(byte sessionId, EndAudioPassThruResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }

        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final Result result = response.getResultCode();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onEndAudioPassThruResponse(result);
            }
        });
    }

    @Override
    public void onOnAudioPassThru(byte sessionId, OnAudioPassThru notification) {
        createDebugMessageForAdapter(sessionId, notification);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        final byte[] aptData = notification.getAPTData();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onAudioPassThru(aptData);
            }
        });
    }

    /**
     * **************************************
     *  SYNC AppLink Vehicle Data Callbacks *
     * **************************************
     */
    @Override
    public void onSubscribeVehicleDataResponse(byte sessionId,
                                               SubscribeVehicleDataResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onUnsubscribeVehicleDataResponse(byte sessionId,
                                                 UnsubscribeVehicleDataResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onGetVehicleDataResponse(byte sessionId, GetVehicleDataResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onReadDIDResponse(byte sessionId, ReadDIDResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onGetDTCsResponse(byte sessionId, GetDTCsResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onOnVehicleData(byte sessionId, OnVehicleData notification) {
        createDebugMessageForAdapter(sessionId, notification);
    }

    /**
     * *******************************
     *  SYNC AppLink TBT Callbacks   *
     * *******************************
     */
    @Override
    public void onShowConstantTBTResponse(byte sessionId, ShowConstantTBTResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onAlertManeuverResponse(byte sessionId, AlertManeuverResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onUpdateTurnListResponse(byte sessionId, UpdateTurnListResponse response) {
        createDebugMessageForAdapter(sessionId, response);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSystemRequestResponse(byte sessionId, SystemRequestResponse response) {
        createDebugMessageForAdapter(sessionId, response);

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(
                    new Pair<Integer, Result>(response.getCorrelationID(),
                            response.getResultCode())
            );
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onMobileNaviStart(String appId, byte sessionId) {
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onServiceStart(ServiceType.Mobile_Nav, sessionId, appId);
        }
    }

    @Override
    public void onAudioServiceStart(String appId, byte sessionId) {
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onServiceStart(ServiceType.Audio_Service, sessionId, appId);
        }
    }

    @Override
    public void onMobileNavAckReceived(String appId, byte sessionId, int frameReceivedNumber) {
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onAckReceived(appId, sessionId, frameReceivedNumber,
                    ServiceType.Mobile_Nav);
        }
    }

    @Override
    public void onStartServiceNackReceived(String appId, byte sessionId, ServiceType serviceType) {
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onStartServiceNackReceived(appId, sessionId, serviceType);
        }
    }

    @Override
    public void onOnTouchEvent(byte sessionId, OnTouchEvent notification) {
        final OnTouchEvent event = notification;
        createDebugMessageForAdapter(notification);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.onTouchEventReceived(event);
            }
        });

    }

    @Override
    public void onKeyboardInput(byte sessionId, OnKeyboardInput msg) {
        final OnKeyboardInput event = msg;
        createDebugMessageForAdapter(sessionId, msg);
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO : SessionId ???
                mainActivity.onKeyboardInputReceived(event);
            }
        });
    }

    @Override
    public void onOnSystemRequest(byte sessionId, OnSystemRequest notification) {
        createDebugMessageForAdapter(sessionId, notification);
    }

    @Override
    public void onRegisterAppRequest(byte sessionId, RegisterAppInterface msg) {
        createDebugMessageForAdapter(sessionId, msg);
    }

    @Override
    public void onAppUnregisteredAfterLanguageChange(byte sessionId, OnLanguageChange msg) {
        String message =
                String.format("OnAppInterfaceUnregistered (LANGUAGE_CHANGE) from %s to %s",
                        msg.getLanguage(), msg.getHmiDisplayLanguage());
        createDebugMessageForAdapter(message);
        mSyncProxy.resetLanguagesDesired(sessionId, msg.getLanguage(), msg.getHmiDisplayLanguage());
    }

    @Override
    public void onAppUnregisteredReason(byte sessionId, AppInterfaceUnregisteredReason reason) {
        createDebugMessageForAdapter("onAppUnregisteredReason:" + reason + ", sesId:" + sessionId);
    }

    @Override
    public void onProtocolServiceEnded(final ServiceType serviceType, final byte sessionId) {
        String response = " EndService received serType:" + serviceType.getName() +
                ", sesId:" + sessionId;
        createDebugMessageForAdapter(sessionId, response);

        endProtocolService(sessionId, serviceType);
    }

    @Override
    public void onProtocolServiceEndedAck(final ServiceType serviceType, final byte sessionId) {
        String response = " EndServiceAck received serType:" + serviceType.getName() +
                ", sesId:" + sessionId;
        createDebugMessageForAdapter(sessionId, response);

        endProtocolService(sessionId, serviceType);
    }

    @Override
    public void onSessionStarted(String appId, byte sessionId) {
        Logger.d(TAG, " SessionStart:" + sessionId + ", AppId:" + appId);
        mSessionsCounter.incrementAndGet();
        appIdToSessionIdMap.put(appId, sessionId);
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onServiceStart(ServiceType.RPC, sessionId, appId);
        }
    }

    @Override
    public void onOnTBTClientState(OnTBTClientState notification) {
        createDebugMessageForAdapter(notification);
    }

    @Override
    public void onStartSession() {
        createMessageForAdapter(Session.DEFAULT_SESSION_ID, " Session going to start, " +
                "protocol version: " + syncProxyGetWiProVersion(), Log.DEBUG);
    }

    /**
     * ******************************
     * * SYNC AppLink Policies Callback's **
     * *******************************
     */
    @Override
    public void onOnPermissionsChange(byte sessionId, OnPermissionsChange notification) {
        createDebugMessageForAdapter(sessionId, notification);
    }

    EncodedSyncPDataHeader encodedSyncPDataHeaderfromGPS;

    @Override
    public void onOnEncodedSyncPData(OnEncodedSyncPData notification) {
        Logger.i("syncp", "MessageType: " + notification.getMessageType());

        createDebugMessageForAdapter(notification);

        EncodedSyncPDataHeader encodedSyncPDataHeader;
        try {
            encodedSyncPDataHeader = EncodedSyncPDataHeader.parseEncodedSyncPDataHeader(
                    Base64.decode(notification.getData().get(0)));
        } catch (IOException e) {
            Logger.e(TAG + " Can't decode base64 string", e);
            return;
        }

        if (encodedSyncPDataHeader.getServiceType() == 3 && encodedSyncPDataHeader.getCommandType() == 1) {
            saveEncodedSyncPData(encodedSyncPDataHeader.getPayload());

            Logger.i("EncodedSyncPDataHeader", "Protocol Version: " + encodedSyncPDataHeader.getProtocolVersion());
            Logger.i("EncodedSyncPDataHeader", "Response Required: " + encodedSyncPDataHeader.getResponseRequired());
            Logger.i("EncodedSyncPDataHeader", "High Bandwidth: " + encodedSyncPDataHeader.getHighBandwidth());
            Logger.i("EncodedSyncPDataHeader", "Signed: " + encodedSyncPDataHeader.getSigned());
            Logger.i("EncodedSyncPDataHeader", "Encrypted: " + encodedSyncPDataHeader.getEncrypted());
            Logger.i("EncodedSyncPDataHeader", "Payload Size: " + encodedSyncPDataHeader.getPayloadSize());
            Logger.i("EncodedSyncPDataHeader", "Has ESN: " + encodedSyncPDataHeader.getHasESN());
            Logger.i("EncodedSyncPDataHeader", "Service Type: " + encodedSyncPDataHeader.getServiceType());
            Logger.i("EncodedSyncPDataHeader", "Command Type: " + encodedSyncPDataHeader.getCommandType());
            Logger.i("EncodedSyncPDataHeader", "CPU Destination: " + encodedSyncPDataHeader.getCPUDestination());
            Logger.i("EncodedSyncPDataHeader", "Encryption Key Index: " + encodedSyncPDataHeader.getEncryptionKeyIndex());

            byte[] tempESN = encodedSyncPDataHeader.getESN();
            String stringESN = "";
            for (int i = 0; i < 8; i++) stringESN += tempESN[i];
            Logger.i("EncodedSyncPDataHeader", "ESN: " + stringESN);

            try {
                Logger.i("EncodedSyncPDataHeader", "Module Message ID: " + encodedSyncPDataHeader.getModuleMessageID());
            } catch (Exception e) {

            }
            try {
                Logger.i("EncodedSyncPDataHeader", "Server Message ID: " + encodedSyncPDataHeader.getServerMessageID());
            } catch (Exception e) {

            }
            try {
                Logger.i("EncodedSyncPDataHeader", "Message Status: " + encodedSyncPDataHeader.getMessageStatus());
            } catch (Exception e) {

            }

            //create header for syncp packet
            if (encodedSyncPDataHeader.getHighBandwidth()) {
                byte[] tempIV = encodedSyncPDataHeader.getIV();
                String stringIV = "";
                for (int i = 0; i < 16; i++) stringIV += tempIV[i];
                Logger.i("EncodedSyncPDataHeader", "IV: " + stringIV);

                byte[] tempPayload = encodedSyncPDataHeader.getPayload();
                String stringPayload = "";
                for (int i = 0; i < encodedSyncPDataHeader.getPayloadSize(); i++)
                    stringPayload += tempPayload[i];
                Logger.i("EncodedSyncPDataHeader", "Payload: " + stringPayload);

                byte[] tempSignatureTag = encodedSyncPDataHeader.getSignatureTag();
                String stringSignatureTag = "";
                for (int i = 0; i < 16; i++) stringSignatureTag += tempSignatureTag[i];
                Logger.i("EncodedSyncPDataHeader", "Signature Tag: " + stringSignatureTag);
            } else {
                byte[] tempIV = encodedSyncPDataHeader.getIV();
                String stringIV = "";
                for (int i = 0; i < 8; i++) stringIV += tempIV[i];
                Logger.i("EncodedSyncPDataHeader", "IV: " + stringIV);

                byte[] tempPayload = encodedSyncPDataHeader.getPayload();
                String stringPayload = "";
                for (int i = 0; i < encodedSyncPDataHeader.getPayloadSize(); i++)
                    stringPayload += tempPayload[i];
                Logger.i("EncodedSyncPDataHeader", "Payload: " + stringPayload);

                byte[] tempSignatureTag = encodedSyncPDataHeader.getSignatureTag();
                String stringSignatureTag = "";
                for (int i = 0; i < 8; i++) stringSignatureTag += tempSignatureTag[i];
                Logger.i("EncodedSyncPDataHeader", "Signature Tag: " + stringSignatureTag);
            }

            encodedSyncPDataHeaderfromGPS = encodedSyncPDataHeader;
            SyncProxyTester.setESN(tempESN);
            if (PoliciesTesterActivity.getInstance() != null) {
                PoliciesTesterActivity.setESN(tempESN);
                PoliciesTesterActivity.setHeader(encodedSyncPDataHeader);
            }
        }

        if (encodedSyncPDataHeader.getServiceType() == 7) {
            saveEncodedSyncPData(encodedSyncPDataHeader.getPayload());
        }
    }

    private void saveEncodedSyncPData(byte[] data) {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/policiesResults.txt";
        AppUtils.saveDataToFile(data, filePath);
    }

    @Override
    public IBinder onBind(Intent intent) {
        createInfoMessageForAdapter("Service on bind");
        return mBinder;
    }

    @Override
    public void onRegisterAppInterfaceResponse(byte sessionId, RegisterAppInterfaceResponse response) {
        createDebugMessageForAdapter(sessionId, response);

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(), response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }

        try {
            processRegisterAppInterfaceResponse(sessionId, response);
        } catch (SyncException e) {
            createErrorMessageForAdapter("Can not process RAIResponse:" + e.getMessage());
        }
    }

    @Override
    public void onUnregisterAppInterfaceResponse(byte sessionId,
                                                 UnregisterAppInterfaceResponse response) {
        createDebugMessageForAdapter(sessionId, response);

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onSyncPDataResponse(byte sessionId, SyncPDataResponse response) {
        createDebugMessageForAdapter(sessionId, response);

        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(response.getCorrelationID(),
                    response.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    @Override
    public void onOnSyncPData(OnSyncPData notification) {
        createDebugMessageForAdapter(notification);
    }

    private void resendUnsentPutFiles() {
        SparseArray<PutFile> unsentPutFiles = mPutFileTransferManager.getCopy();
        mPutFileTransferManager.clear();
        for (int i = 0; i < unsentPutFiles.size(); i++) {
            commandPutFile(unsentPutFiles.valueAt(i));
        }
        unsentPutFiles.clear();
    }

    private void stopServiceBySelf() {
        Logger.i(TAG + " Stop Service By Self");
        stopSelf();
    }

    // TODO : Set command factory in separate place
    /**
     * Commands Section
     */

    /**
     * Create and send ListFiles command
     */

    /**
     * Create and send ListFiles command
     */
    public void commandListFiles() {
        try {
            mSyncProxy.listFiles(getNextCorrelationID());
            createMessageForAdapter("ListFiles sent", Log.DEBUG);
        } catch (SyncException e) {
            createErrorMessageForAdapter("ListFiles send error: " + e);
        }
    }

    /**
     * Create and send PutFile command
     *
     * @param putFile PurFile to be send
     */
    public void commandPutFile(PutFile putFile) {
        commandPutFile(null, null, null, getNextCorrelationID(), null, putFile);
    }

    /**
     * Create and send PutFile command
     *
     * @param fileType     Type of the File
     * @param syncFileName Name of the File
     * @param bulkData     Data of the File
     */
    public void commandPutFile(FileType fileType, String syncFileName, byte[] bulkData) {
        commandPutFile(fileType, syncFileName, bulkData, -1, null, null);
    }

    /**
     * Create and send PutFile command
     *
     * @param fileType      Type of the File
     * @param syncFileName  Name of the File
     * @param bulkData      Data of the File
     * @param correlationId Unique identifier of the command
     */
    public void commandPutFile(FileType fileType, String syncFileName, byte[] bulkData,
                               int correlationId) {
        commandPutFile(fileType, syncFileName, bulkData, correlationId, null, null);
    }

    /**
     * Create and send PutFile command
     *
     * @param fileType        Type of the File
     * @param syncFileName    Name of the File
     * @param bulkData        Data of the File
     * @param correlationId   Unique identifier of the command
     * @param doSetPersistent
     */
    public void commandPutFile(FileType fileType, String syncFileName, byte[] bulkData,
                               int correlationId, Boolean doSetPersistent) {
        commandPutFile(fileType, syncFileName, bulkData, correlationId, doSetPersistent, null);
    }

    /**
     * Create and send PutFile command
     *
     * @param fileType        Type of the File
     * @param syncFileName    Name of the File
     * @param bulkData        Data of the File
     * @param correlationId   Unique identifier of the command
     * @param doSetPersistent
     * @param putFile         PurFile to be send
     */
    public void commandPutFile(FileType fileType, String syncFileName,
                               byte[] bulkData, int correlationId,
                               Boolean doSetPersistent, PutFile putFile) {
        commandPutFile(fileType, syncFileName, bulkData, correlationId,
                doSetPersistent, null, null, null, putFile);
    }

    /**
     * Create and send PutFile command
     *
     * @param fileType        Type of the File
     * @param syncFileName    Name of the File
     * @param bulkData        Data of the File
     * @param correlationId   Unique identifier of the command
     * @param doSetPersistent
     * @param isSystemFile
     * @param length
     * @param offset
     * @param putFile         PurFile to be send
     */
    public void commandPutFile(FileType fileType, String syncFileName,
                               byte[] bulkData, int correlationId,
                               Boolean doSetPersistent, Boolean isSystemFile,
                               Integer length, Integer offset,
                               PutFile putFile) {
        int mCorrelationId = correlationId;
        if (correlationId == -1) {
            mCorrelationId = getNextCorrelationID();
        }

        PutFile newPutFile = RPCRequestFactory.buildPutFile();

        if (putFile == null) {
            newPutFile.setFileType(fileType);
            newPutFile.setSyncFileName(syncFileName);
            if (doSetPersistent != null) {
                newPutFile.setPersistentFile(doSetPersistent);
            }

            if (isSystemFile != null) {
                newPutFile.setSystemFile(isSystemFile);
            }

            if (length != null) {
                newPutFile.setLength(length);
            }

            if (offset != null) {
                newPutFile.setOffset(offset);
            }

            newPutFile.setBulkData(bulkData);
        } else {
            newPutFile = putFile;
        }

        newPutFile.setCorrelationID(mCorrelationId);

        mPutFileTransferManager.addPutFileToAwaitArray(mCorrelationId, newPutFile);

        syncProxySendPutFilesResumable(newPutFile);
    }

    /**
     * Call a method from SDK to send <b>SubscribeButton</b> request
     *
     * @param buttonName {@link com.ford.syncV4.proxy.rpc.enums.ButtonName}
     */
    public void commandSubscribeButtonPredefined(ButtonName buttonName, int correlationId) {
        SubscribeButton subscribeButton = RPCRequestFactory.buildSubscribeButton();
        subscribeButton.setCorrelationID(correlationId);
        subscribeButton.setButtonName(buttonName);

        syncProxySendRPCRequestWithPreprocess(subscribeButton);
    }

    /**
     * Call a method from SDK to send <b>SubscribeButton</b> request which will be used in application
     * resumption.
     *
     * @param correlationId Unique identifier of the command
     * @param buttonName    {@link com.ford.syncV4.proxy.rpc.enums.ButtonName}
     */
    public void commandSubscribeButtonResumable(ButtonName buttonName, int correlationId) {
        SubscribeButton subscribeButton = RPCRequestFactory.buildSubscribeButton();
        subscribeButton.setCorrelationID(correlationId);
        subscribeButton.setButtonName(buttonName);

        syncProxySendRPCRequestResumable(subscribeButton);
    }

    /**
     * Call a method from SDK to send <b>UnsubscribeVehicleData</b> request.
     *
     * @param unsubscribeVehicleData {@link com.ford.syncV4.proxy.rpc.UnsubscribeVehicleData}
     */
    public void commandUnsubscribeVehicleInterface(UnsubscribeVehicleData unsubscribeVehicleData) {
        syncProxySendRPCRequestWithPreprocess(unsubscribeVehicleData);
    }

    /**
     * Call a method from SDK to send <b>SubscribeVehicleData</b> request which will be used in
     * application resumption.
     *
     * @param subscribeVehicleData {@link com.ford.syncV4.proxy.rpc.SubscribeVehicleData}
     */
    public void commandSubscribeVehicleInterfaceResumable(SubscribeVehicleData subscribeVehicleData) {
        syncProxySendRPCRequestResumable(subscribeVehicleData);
    }

    /**
     * Call a method from SDK to send <b>AddCommand</b> request which will be used in application
     * resumption.
     *
     * @param commandId  Id of the command
     * @param vrCommands Vector of the VR Commands
     * @param menuName   Name of the Menu
     */
    public void commandAddCommandResumable(Integer commandId, Vector<String> vrCommands,
                                           String menuName) {
        AddCommand addCommand = RPCRequestFactory.buildAddCommand(commandId, menuName, vrCommands,
                getNextCorrelationID());
        syncProxySendRPCRequestResumable(addCommand);
    }

    /**
     * Call a method from SDK to send <b>AddCommand</b> request
     *
     * @param commandId  Id of the command
     * @param vrCommands Vector of the VR Commands
     * @param menuName   Name of the Menu
     */
    public void commandAddCommandPredefined(Integer commandId, Vector<String> vrCommands,
                                            String menuName) {
        AddCommand addCommand = RPCRequestFactory.buildAddCommand(commandId, menuName, vrCommands,
                getNextCorrelationID());
        syncProxySendRPCRequestWithPreprocess(addCommand);
    }

    /**
     * Call a method from SDK to send <b>AddCommand</b> request which will be used in application
     * resumption.
     *
     * @param addCommand {@link com.ford.syncV4.proxy.rpc.AddCommand} object
     */
    public void commandAddCommandResumable(AddCommand addCommand) {
        syncProxySendRPCRequestResumable(addCommand);
    }

    /**
     * Call a method from SDK to send <b>AddSubMenu</b> request which will be used in application
     * resumption.
     *
     * @param setGlobalProperties {@link com.ford.syncV4.proxy.rpc.SetGlobalProperties}
     */
    public void commandSetGlobalPropertiesResumable(SetGlobalProperties setGlobalProperties) {
        syncProxySendRPCRequestResumable(setGlobalProperties);
    }

    /**
     * Call a method from SDK to send <b>AddSubMenu</b> request which will be used in application
     * resumption.
     *
     * @param addSubMenu {@link com.ford.syncV4.proxy.rpc.AddSubMenu} object
     */
    public void commandAddSubMenuResumable(AddSubMenu addSubMenu) {
        syncProxySendRPCRequestResumable(addSubMenu);
    }

    /**
     * Call a method from SDK to create and send <b>CreateInteractionChoiceSet</b> request which
     * will be used in application resumption.
     *
     * @param choiceSet              Set of the {@link com.ford.syncV4.proxy.rpc.Choice} objects
     * @param interactionChoiceSetID Id of the interaction Choice set
     * @param correlationID          correlation Id
     */
    public void commandCreateInteractionChoiceSetResumable(Vector<Choice> choiceSet,
                                                           Integer interactionChoiceSetID,
                                                           Integer correlationID) {

        CreateInteractionChoiceSet createInteractionChoiceSet =
                RPCRequestFactory.buildCreateInteractionChoiceSet(choiceSet,
                        interactionChoiceSetID, correlationID);
        syncProxySendRPCRequestResumable(createInteractionChoiceSet);
    }

    /**
     * SyncProxy section, transfer call methods from Application to SyncProxy
     */

    public void syncProxyStopAudioDataTransfer() {
        if (mSyncProxy != null) {
            mSyncProxy.stopAudioDataTransfer();
        }
    }

    public void syncProxyStopH264() {
        if (mSyncProxy != null) {
            mSyncProxy.stopH264();
        }
    }

    public void syncProxyCloseSession(String appId) throws SyncException {
        if (mSyncProxy != null) {
            mSyncProxy.closeSession(appId, false);
        }
    }

    public void syncProxyOpenSession(String syncAppId) throws SyncException {
        if (mSyncProxy != null) {
            if (mSyncProxy.getIsConnected()) {

                RegisterAppInterface registerAppInterface = registerAppInterfaceHashMap.get(syncAppId);
                if (registerAppInterface == null) {

                    SharedPreferences settings = getSharedPreferences(Const.PREFS_NAME, 0);
                    boolean isMediaApp = settings.getBoolean(
                            Const.PREFS_KEY_ISMEDIAAPP, Const.PREFS_DEFAULT_ISMEDIAAPP);
                    boolean isNaviApp = settings.getBoolean(
                            Const.PREFS_KEY_ISNAVIAPP, Const.PREFS_DEFAULT_ISNAVIAPP);

                    Vector<AppHMIType> appHMITypes = createAppTypeVector(isNaviApp);

                    // Apply custom AppId in case of such possibility selected
                    TransportType transportType = AppPreferencesManager.getTransportType();
                    String appId = AppIdManager.getAppIdByTransport(transportType);
                    if (AppPreferencesManager.getIsCustomAppId()) {
                        appId = AppPreferencesManager.getCustomAppId();
                    }

                    String appName = settings.getString(Const.PREFS_KEY_APPNAME,
                            Const.PREFS_DEFAULT_APPNAME);
                    Language lang = Language.valueOf(settings.getString(
                            Const.PREFS_KEY_LANG, Const.PREFS_DEFAULT_LANG));
                    Language hmiLang = Language.valueOf(settings.getString(
                            Const.PREFS_KEY_HMILANG, Const.PREFS_DEFAULT_HMILANG));

                    registerAppInterface = RPCRequestFactory.buildRegisterAppInterface();
                    registerAppInterface.setAppName(appName);
                    registerAppInterface.setLanguageDesired(lang);
                    registerAppInterface.setHmiDisplayLanguageDesired(hmiLang);
                    registerAppInterface.setAppID(appId);
                    registerAppInterface.setIsMediaApplication(isMediaApp);
                    registerAppInterface.setAppType(appHMITypes);

                    SyncMsgVersion syncMsgVersion = new SyncMsgVersion();
                    syncMsgVersion.setMajorVersion(2);
                    syncMsgVersion.setMinorVersion(2);
                    registerAppInterface.setSyncMsgVersion(syncMsgVersion);

                    registerAppInterfaceHashMap.put(syncAppId, registerAppInterface);

                    mSyncProxy.updateRegisterAppInterfaceParameters(registerAppInterface);
                    mSyncProxy.openSession();
                }

                // TODO : Implement when reconnect
                /*for (String key : registerAppInterfaceHashMap.keySet()) {
                    registerAppInterface = registerAppInterfaceHashMap.get(key);
                    mSyncProxy.updateRegisterAppInterfaceParameters(registerAppInterface);
                    mSyncProxy.openSession();
                }*/

            } else {
                mSyncProxy.initializeProxy();
            }
        } else {
            Logger.w(TAG + " OpenSession, proxy NULL");
        }
    }

    public void syncProxyStartAudioService(byte sessionId) {
        // TODO : Set 'startAudioService' in the SyncProxyBase
        if (mSyncProxy != null && mSyncProxy.getSyncConnection() != null) {
            mSyncProxy.getSyncConnection().startAudioService(sessionId);
        }
    }

    public void syncProxyStopAudioService(byte sessionId) {
        if (mSyncProxy != null) {
            mSyncProxy.stopAudioService(sessionId);
        }
    }

    public OutputStream syncProxyStartAudioDataTransfer(byte sessionId) {
        if (mSyncProxy != null) {
            return mSyncProxy.startAudioDataTransfer(sessionId);
        }
        return null;
    }

    /**
     * This method is send RPC Request to the Sync Proxy
     *
     * @param request object of {@link com.ford.syncV4.proxy.RPCRequest} type
     */
    public void syncProxySendRPCRequestWithPreprocess(RPCRequest request) {
        if (request == null) {
            createErrorMessageForAdapter("RPC request is NULL");
            return;
        }
        try {
            if (request.getFunctionName().equals(Names.RegisterAppInterface)) {
                syncProxySendRegisterRequest((RegisterAppInterface) request);
            } else {
                createDebugMessageForAdapter(request);
                mSyncProxy.sendRPCRequest(request);
            }
        } catch (SyncException e) {
            createErrorMessageForAdapter("RPC request '" + request.getFunctionName() + "'" +
                    " send error");
        }
    }

    public void syncProxySendRPCRequest(RPCRequest request) {
        if (request == null) {
            createErrorMessageForAdapter("RPC request is NULL");
            return;
        }
        try {
            createDebugMessageForAdapter(request);
            mSyncProxy.sendRPCRequest(request);
        } catch (SyncException e) {
            createErrorMessageForAdapter("RPC request '" + request.getFunctionName() + "'" +
                    " send error");
        }
    }

    /**
     * This method is for the requests on which resumption is depends on. All the requests will be
     * stored in the collection in order to re-use them when resumption will have place.
     *
     * @param request {@link com.ford.syncV4.proxy.RPCRequest} object
     */
    public void syncProxySendRPCRequestResumable(RPCRequest request) {
        if (request == null) {
            createErrorMessageForAdapter("Resumable RPC request is NULL");
            return;
        }

        if (mSyncProxy.getIsConnected()) {
            mRpcRequestsResumableManager.addRequestConnected(request);
        } else {
            mRpcRequestsResumableManager.addRequestDisconnected(request);
        }

        syncProxySendRPCRequestWithPreprocess(request);
    }

    /**
     * @param putFile
     */
    public void syncProxySendPutFilesResumable(PutFile putFile) {
        if (putFile == null) {
            createErrorMessageForAdapter("Resumable PuFile is NULL");
            return;
        }

        //mRpcRequestsResumableManager.addPutFile(putFile);

        syncProxySendRPCRequestWithPreprocess(putFile);
    }

    private void syncProxySendRegisterRequest(RegisterAppInterface msg) throws SyncException {
        if (mSyncProxy == null) {
            return;
        }

        if (mSyncProxy.getSyncConnection() == null) {
            return;
        }

        // TODO it's seems stupid in order to register send onTransportConnected
        mSyncProxy.updateRegisterAppInterfaceParameters(msg);

        mSyncProxy.getSyncConnection().onTransportConnected();
    }

    public byte syncProxyGetWiProVersion() {
        if (mSyncProxy != null && mSyncProxy.getSyncConnection() != null) {
            return mSyncProxy.getSyncConnection().getProtocolVersion();
        }
        return ProtocolConstants.PROTOCOL_VERSION_UNDEFINED;
    }

    public void syncProxyStartMobileNavService(byte sessionId) {
        if (mSyncProxy != null && mSyncProxy.getSyncConnection() != null) {
            mSyncProxy.getSyncConnection().startMobileNavService(sessionId);
        }
    }

    public OutputStream syncProxyStartH264(byte sessionId) {
        if (mSyncProxy != null) {
            return mSyncProxy.startH264(sessionId);
        }
        return null;
    }

    public void syncProxyStopMobileNaviService(byte sessionId) {
        if (mSyncProxy != null) {
            mSyncProxy.stopMobileNaviService(sessionId);
        }
    }

    public IJsonRPCMarshaller syncProxyGetJsonRPCMarshaller() {
        if (mSyncProxy != null) {
            return mSyncProxy.getJsonRPCMarshaller();
        }
        return null;
    }

    public void syncProxySetJsonRPCMarshaller(IJsonRPCMarshaller jsonRPCMarshaller) {
        if (mSyncProxy != null) {
            mSyncProxy.setJsonRPCMarshaller(jsonRPCMarshaller);
        }
    }

    private void setAppIcon() {
        SetAppIcon setAppIcon = RPCRequestFactory.buildSetAppIcon();
        setAppIcon.setSyncFileName(ICON_SYNC_FILENAME);
        setAppIcon.setCorrelationID(getNextCorrelationID());

        syncProxySendRPCRequestWithPreprocess(setAppIcon);
    }

    private void endProtocolService(byte sessionId, ServiceType serviceType) {
        if (mProxyServiceEvent != null) {
            mProxyServiceEvent.onServiceEnd(serviceType);
        }
        if (serviceType == ServiceType.RPC) {
            createDebugMessageForAdapter(sessionId, " RPC service stopped");

            String appIdToRemove = "";
            for (String key: appIdToSessionIdMap.keySet()) {
                if (appIdToSessionIdMap.get(key) == sessionId) {
                    appIdToRemove = key;
                    break;
                }
            }

            appIdToSessionIdMap.remove(appIdToRemove);

            if (registerAppInterfaceHashMap.containsKey(appIdToRemove)) {
                RegisterAppInterface result = registerAppInterfaceHashMap.remove(appIdToRemove);
                Logger.d("RAI object has been removed:" + result);
            }

            if (mProxyServiceEvent != null) {
                if (mSessionsCounter.decrementAndGet() == 0) {
                    mProxyServiceEvent.onDisposeComplete();
                }
            }

            if (mCloseSessionCallback != null) {
                mCloseSessionCallback.onCloseSessionComplete();
            }
        }
    }

    /**
     * Process a response of the {@link com.ford.syncV4.proxy.rpc.RegisterAppInterface} request
     *
     * @param response {@link com.ford.syncV4.proxy.rpc.RegisterAppInterfaceResponse} object
     */
    private void processRegisterAppInterfaceResponse(byte sessionId,
                                                     RegisterAppInterfaceResponse response)
            throws SyncException {

        if (!response.getSuccess()) {
            return;
        }

        if (response.getResultCode() == Result.SUCCESS) {
            //mRpcRequestsResumableManager.sendAllPutFiles();
            mRpcRequestsResumableManager.sendAllRequestsDisconnected();
        } else if (response.getResultCode() == Result.RESUME_FAILED) {
            //mRpcRequestsResumableManager.sendAllPutFiles();
            mRpcRequestsResumableManager.sendAllRequestsConnected();
            mRpcRequestsResumableManager.sendAllRequestsDisconnected();
        }

        //mRpcRequestsResumableManager.cleanAllPutFiles();
        mRpcRequestsResumableManager.cleanAllRequestsConnected();
        mRpcRequestsResumableManager.cleanAllRequestsDisconnected();

        // Restore a PutFile which has not been sent
        resendUnsentPutFiles();
        // Restore Services
        mSyncProxy.restoreServices(sessionId);
    }

    // TODO: Reconsider this section, this is a first step to optimize log procedure

    /**
     * Logger section. Send log message to adapter and log it to the ADB
     */

    private void createErrorMessageForAdapter(byte sessionId, Object messageObject) {
        createErrorMessageForAdapter(sessionId, messageObject, null);
    }

    private void createErrorMessageForAdapter(Object messageObject) {
        createErrorMessageForAdapter(messageObject, null);
    }

    private void createErrorMessageForAdapter(Object messageObject, Throwable throwable) {
        createErrorMessageForAdapter(Session.UNDEFINED_SESSION_ID, messageObject, throwable);
    }

    private void createErrorMessageForAdapter(byte sessionId, Object messageObject, Throwable throwable) {
        if (mLogAdapters.isEmpty()) {
            Logger.w(TAG, "LogAdapters are empty, sessionId:" + sessionId);
            if (throwable != null) {
                Logger.e(TAG + " " + messageObject.toString(), throwable);
            } else {
                Logger.e(TAG, messageObject.toString());
            }
            return;
        }
        for (LogAdapter logAdapter : mLogAdapters) {
            if (sessionId == Session.UNDEFINED_SESSION_ID) {
                if (throwable != null) {
                    logAdapter.logMessage(messageObject, Log.ERROR, throwable, true);
                } else {
                    logAdapter.logMessage(messageObject, Log.ERROR, true);
                }
            } else {
                if (logAdapter.getSessionId() == sessionId) {
                    if (throwable != null) {
                        logAdapter.logMessage(messageObject, Log.ERROR, throwable, true);
                    } else {
                        logAdapter.logMessage(messageObject, Log.ERROR, true);
                    }
                }
            }
        }
    }

    private void createInfoMessageForAdapter(Object messageObject) {
        createMessageForAdapter(Session.UNDEFINED_SESSION_ID, messageObject, Log.INFO);
    }

    private void createDebugMessageForAdapter(byte sessionId, Object messageObject) {
        createMessageForAdapter(sessionId, messageObject, Log.DEBUG);
    }

    private void createDebugMessageForAdapter(Object messageObject) {
        createMessageForAdapter(Session.UNDEFINED_SESSION_ID, messageObject, Log.DEBUG);
    }

    private void createMessageForAdapter(Object messageObject, Integer type) {
        createMessageForAdapter(Session.UNDEFINED_SESSION_ID, messageObject, type);
    }

    private void createMessageForAdapter(byte sessionId, Object messageObject, Integer type) {
        if (mLogAdapters.isEmpty()) {
            Logger.w(TAG, "LogAdapters are empty, sessionId:" + sessionId);
            if (type == Log.DEBUG) {
                Logger.d(TAG, messageObject.toString());
            } else if (type == Log.INFO) {
                Logger.i(TAG, messageObject.toString());
            }
            return;
        }
        for (LogAdapter logAdapter : mLogAdapters) {
            if (sessionId == Session.UNDEFINED_SESSION_ID) {
                logAdapter.logMessage(messageObject, type, true);
            } else {
                if (logAdapter.getSessionId() == sessionId) {
                    logAdapter.logMessage(messageObject, type, true);
                }
            }
        }
    }

    @Override
    public void onUSBNoSuchDeviceException() {
        final SyncProxyTester mainActivity = SyncProxyTester.getInstance();
        if (mainActivity != null) {
            mainActivity.onUSBNoSuchDeviceException();
        }
    }

    @Override
    public void onDiagnosticMessageResponse(byte sessionId,
                                            DiagnosticMessageResponse diagnosticMessageResponse) {
        createDebugMessageForAdapter(sessionId, diagnosticMessageResponse);
        if (isModuleTesting()) {
            ModuleTest.sResponses.add(new Pair<Integer, Result>(
                    diagnosticMessageResponse.getCorrelationID(),
                    diagnosticMessageResponse.getResultCode()));
            synchronized (mTesterMain.getXMLTestThreadContext()) {
                mTesterMain.getXMLTestThreadContext().notify();
            }
        }
    }

    /**
     * Test Section
     */

    @Override
    public void onRPCServiceComplete() {
        if (isModuleTesting() && !mTestConfig.isDoCallRegisterAppInterface()) {
            synchronized (mTesterMain.getTestActionThreadContext()) {
                mTesterMain.getTestActionThreadContext().notify();
            }
        }
    }
}