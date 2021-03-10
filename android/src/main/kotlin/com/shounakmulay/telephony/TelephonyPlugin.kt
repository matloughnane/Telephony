package com.shounakmulay.telephony

import android.content.Context
import androidx.annotation.NonNull
import com.shounakmulay.telephony.sms.IncomingSmsHandler
import com.shounakmulay.telephony.utils.Constants.CHANNEL_SMS
import com.shounakmulay.telephony.sms.IncomingSmsReceiver
import com.shounakmulay.telephony.sms.SmsController
import com.shounakmulay.telephony.sms.SmsMethodCallHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*


class TelephonyPlugin : FlutterPlugin, ActivityAware {

  private lateinit var smsChannel: MethodChannel

  private lateinit var smsMethodCallHandler: SmsMethodCallHandler

  private lateinit var smsController: SmsController

  private lateinit var binaryMessenger: BinaryMessenger

  // public TelephonyPlugin() {}

  // public static void registerWith(Registrar registrar) {
  //   TelephonyPlugin instance = new TelephonyPlugin();
  //   instance.channel = new MethodChannel(registrar.messenger(), CHANNEL_TELEPHONY);
  //   instance.context = registrar.context();
  //   instance.channel.setMethodCallHandler(instance);
  // }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val isInForeground = IncomingSmsHandler.isApplicationForeground(flutterPluginBinding.applicationContext);
    if (!this::binaryMessenger.isInitialized && isInForeground) {
      binaryMessenger = flutterPluginBinding.binaryMessenger
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    tearDownPlugin()
  }

  override fun onDetachedFromActivity() {
    tearDownPlugin()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    setupPlugin(binding.activity.applicationContext, binaryMessenger)
    PermissionsController.setActivity(binding.activity)
    binding.addRequestPermissionsResultListener(smsMethodCallHandler)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  private fun setupPlugin(context: Context, messenger: BinaryMessenger) {
    smsController = SmsController(context)
    smsMethodCallHandler = SmsMethodCallHandler(context, smsController)

    smsChannel = MethodChannel(messenger, CHANNEL_SMS)
    smsChannel.setMethodCallHandler(smsMethodCallHandler)
    smsMethodCallHandler.setForegroundChannel(smsChannel)

    IncomingSmsReceiver.foregroundSmsChannel = smsChannel
  }

  private fun tearDownPlugin() {
    IncomingSmsReceiver.foregroundSmsChannel = null
    smsChannel.setMethodCallHandler(null)
  }

  companion object {
    var pluginRegistryCallback: PluginRegistry.PluginRegistrantCallback? = null

    @JvmStatic
    private fun registerTelephony(messenger: BinaryMessenger, ctx: Context) {
        val channel = MethodChannel(messenger, CHANNEL_TELEPHONY)
        channel.setMethodCallHandler(TelephonyPlugin().apply { telephonyCallHandler = TelephonyCallHandler(ctx) })
    }

    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) =
            registerTelephony(registrar.messenger(), registrar.activeContext())

    @Deprecated(message = "Use the Android v2 embedding method.")
    @JvmStatic
    fun setPluginRegistrantCallback(pluginRegistryCallback: PluginRegistry.PluginRegistrantCallback) {
        TelephonyPlugin.pluginRegistryCallback = pluginRegistryCallback
    }
  }

}
