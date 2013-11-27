/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.uiautomation.ios;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.logging.LoggingPreferences;
import org.uiautomation.ios.communication.device.DeviceType;
import org.uiautomation.ios.communication.device.DeviceVariation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class IOSCapabilities extends DesiredCapabilities {

  // UIAutomation properties called from instuments
  // UIAAplication.bundleID();
  // UIATarget.systemName();
  public static final String UI_SYSTEM_NAME = "systemName";
  // UIATarget.systemVersion();
  public static final String UI_SDK_VERSION = "sdkVersion";
  // UIATarget.name();
  public static final String UI_NAME = "name";
  // UIAAplication.bundleVersion();
  public static final String UI_BUNDLE_VERSION = "bundleVersion";
  // UIAAplication.version();
  public static final String UI_VERSION = "version";

  // plist + envt variable
  public static final String DEVICE = "device";
  public static final String VARIATION = "variation";
  public static final String SIMULATOR = "simulator";

  public static final String IOS_SWITCHES = "ios.switches";
  public static final String LANGUAGE = "language";
  public static final String SUPPORTED_LANGUAGES = "supportedLanguages";
  public static final String SUPPORTED_DEVICES = "deviceAlt";
  public static final String LOCALE = "locale";
  public static final String AUT = "aut";
  public static final String TIME_HACK = "timeHack";

  public static final String BUNDLE_VERSION = "CFBundleVersion";
  public static final String BUNDLE_ID = "CFBundleIdentifier";
  public static final String BUNDLE_SHORT_VERSION = "CFBundleShortVersionString";
  public static final String BUNDLE_DISPLAY_NAME = "CFBundleDisplayName";
  public static final String BUNDLE_NAME = "CFBundleName";
  public static final String DEVICE_FAMILLY = "UIDeviceFamily";
  // http://developer.apple.com/library/ios/#documentation/general/Reference/InfoPlistKeyReference/Articles/iPhoneOSKeys.html
  public static final String ICON = "CFBundleIconFile";
  public static final String BUNDLE_ICONS = "CFBundleIcons";

  public static final String MAGIC_PREFIX = "plist_";
  public static final String CONFIGURABLE = "configurable";
  public static final String ELEMENT_TREE = "elementTree";
  public static final String IOS_SEARCH_CONTEXT = "iosSearchContext";
  public static final String UUID = "uuid";
  public static final String IOS_TOUCH_SCREEN = "iosTouchScreen";

  // default selenium bindings for mobile safari
  public static final String BROWSER_NAME = "browserName";
  
  // TODO: make a parameter?
  public static final int COMMAND_TIMEOUT_MILLIS = 10 * 60 * 1000; // 10 minutes

  // private final Map<String, Object> raw = new HashMap<String, Object>();

  public IOSCapabilities() {
    setCapability(TIME_HACK, false);
    initDefaultDeviceVariation();
    setCapability(SIMULATOR, true);
  }

  public IOSCapabilities(JSONObject json) throws JSONException {
    Iterator<String> iter = json.keys();
    while (iter.hasNext()) {
      String key = iter.next();
      Object value = json.get(key);
      if (BROWSER_NAME.equalsIgnoreCase(key) && json.isNull(BUNDLE_NAME)) {
        setCapability(BUNDLE_NAME, "Safari");
        if (((String) value).equalsIgnoreCase("iphone")) {
          setCapability(DEVICE, "iphone");
        } else if (((String) value).equalsIgnoreCase("ipad")) {
          setCapability(DEVICE, "ipad");
        }
      } else {
        setCapability(key, decode(value));
      }
    }
    initDefaultDeviceVariation();
  }

  public IOSCapabilities(Map<String, ?> from) {
    for (String key : from.keySet()) {
      setCapability(key, from.get(key));
    }
  }

  public static IOSCapabilities iphone(String bundleName, String bundleVersion) {
    IOSCapabilities res = new IOSCapabilities();
    res.setCapability(DEVICE, DeviceType.iphone);
    res.setCapability(LANGUAGE, "en");
    res.setCapability(LOCALE, "en_GB");
    res.setCapability(BUNDLE_NAME, bundleName);
    res.setCapability(BUNDLE_VERSION, bundleVersion);
    return res;
  }

  public static IOSCapabilities mobileSafariIpad() {
    return IOSCapabilities.ipad("Safari");
  }

  public static IOSCapabilities iphone(String bundleName) {
    IOSCapabilities res = new IOSCapabilities();
    res.setCapability(DEVICE, DeviceType.iphone);
    res.setCapability(LANGUAGE, "en");
    res.setCapability(LOCALE, "en_GB");
    res.setCapability(BUNDLE_NAME, bundleName);
    return res;
  }

  public static IOSCapabilities ipad(String bundleName) {
    IOSCapabilities res = new IOSCapabilities();
    res.setCapability(DEVICE, DeviceType.ipad);
    res.setCapability(LANGUAGE, "en");
    res.setCapability(LOCALE, "en_GB");
    res.setCapability(BUNDLE_NAME, bundleName);
    return res;
  }

  public Boolean isSimulator() {
    Object o = getCapability(SIMULATOR);
    if (o == null) {
      return null;
    } else if (o instanceof Boolean) {
      return (Boolean) o;
    } else {
      return Boolean.parseBoolean((String) o);
    }
  }

  public String getBundleId() {
    Object o = asMap().get(BUNDLE_ID);
    return ((String) o);
  }

  public void setBundleId(String bundleId) {
    setCapability(BUNDLE_ID, bundleId);
  }

  public String getBundleName() {
    Object o = asMap().get(BUNDLE_NAME);
    return ((String) o);
  }

  public void setBundleName(String bundleName) {
    setCapability(BUNDLE_NAME, bundleName);
  }

  public String getBundleVersion() {
    Object o = asMap().get(BUNDLE_VERSION);
    return ((String) o);
  }

  private Object decode(Object o) throws JSONException {
    if (o instanceof JSONArray) {
      List<Object> res = new ArrayList<Object>();
      JSONArray array = (JSONArray) o;
      for (int i = 0; i < array.length(); i++) {
        Object r = array.get(i);
        res.add(decode(r));
      }
      return res;
    } else {
      return o;
    }
  }

  public Map<String, Object> getRawCapabilities() {
    return (Map<String, Object>) asMap();
  }

  public List<DeviceType> getSupportedDevicesFromDeviceFamily() {
    JSONArray o = (JSONArray) asMap().get(DEVICE_FAMILLY);
    List<DeviceType> devices = new ArrayList<>();
    for (int i = 0; i < o.length(); i++) {
      try {
        devices.add(DeviceType.getFromFamilyCode(o.getInt(i)));
      } catch (JSONException e) {
        throw new WebDriverException(o.toString() + " but should contain only 1 or 2.");
      }
    }
    return devices;
  }

  public DeviceType getDevice() {
    Object o = getCapability(DEVICE);
    return DeviceType.valueOf(o);
  }

 

  public String getSDKVersion() {
    Object o = getCapability(UI_SDK_VERSION);
    return ((String) o);
  }



  public String getApplication() {
    Object o = getCapability(AUT);
    return ((String) o);
  }

  public String getLocale() {
    Object o = getCapability(LOCALE);

    if (o == null) {
      return Locale.getDefault().toString();
    } else {
      return o.toString();
    }
  }

  public void setLocale(String locale) {
    setCapability(LOCALE, locale);
  }

  public String getLanguage() {
    Object o = getCapability(LANGUAGE);
    return ((String) o);
  }

  public void setLanguage(String language) {
    setCapability(LANGUAGE, language);
  }

  public List<String> getExtraSwitches() {
    List<String> res = new ArrayList<String>();
    if (getCapability(IOS_SWITCHES) != null) {
      res.addAll(getList(IOS_SWITCHES));
    }
    return res;
  }

  public boolean isTimeHack() {
    Object o = getCapability(TIME_HACK);
    if (o == null) {
      return false;
    } else if (o instanceof Boolean) {
      return (Boolean) o;
    } else {
      return Boolean.parseBoolean((String) o);
    }
  }

  private List<String> getList(String key) {
    Object o = getCapability(key);
    if (o instanceof List<?>) {
      return Lists.transform((List<?>) o, new Function<Object, String>() {
        @Override
        public String apply(Object o) {
          if (o == null) {
            return null;
          }
          return o.toString();
        }
      });
    } else if (o instanceof JSONArray) {
      JSONArray a = (JSONArray) o;
      List<String> res = new ArrayList<>();

      for (int i = 0; i < a.length(); i++) {
        try {
          res.add(a.getString(i));
        } catch (JSONException e) {
          throw new WebDriverException(e);
        }
      }
      return res;
    }
    throw new ClassCastException("Expected collection of string, got " + o.getClass());
  }

  public List<String> getSupportedLanguages() {
    return getList(SUPPORTED_LANGUAGES);
  }

  

  public List<DeviceType> getSupportedDevices() {
    return Lists.transform(getList(SUPPORTED_DEVICES), new Function<String, DeviceType>() {
      @Override
      public DeviceType apply(String capability) {
        return DeviceType.valueOf(capability);
      }
    });
  }

  public void setSupportedLanguages(List<String> supportedLanguages) {
    setCapability(SUPPORTED_LANGUAGES, supportedLanguages);
  }


  public Object getCapability(String key) {
    Object o = getRawCapabilities().get(key);
    if (o != null && o.equals(JSONObject.NULL)) {
      return null;
    } else {
      return o;
    }
  }

  private void initDefaultDeviceVariation() {
    if (getCapability(DEVICE) != null && getDeviceVariation() == null) {
      switch (getDevice()) {
        case iphone:
          setDeviceVariation(DeviceVariation.iPhoneRetina);
          break;
        case ipad:
          setDeviceVariation(DeviceVariation.iPad);
          break;
      }
    }
  }

  public DeviceVariation getDeviceVariation() {
    Object o = getCapability(VARIATION);
    return o == null ? null : DeviceVariation.valueOf(o);
  }

  public void setDeviceVariation(DeviceVariation variation) {
    setCapability(VARIATION, variation);
  }

  public String getDeviceUUID() {
    return (String) getCapability(UUID);
  }

  public void setDeviceUUID(String deviceUUID) {
    setCapability(UUID, deviceUUID);
  }

  public LoggingPreferences getLoggingPreferences() throws JSONException {
    LoggingPreferences ret = new LoggingPreferences();
    JSONObject json = (JSONObject) getCapability(CapabilityType.LOGGING_PREFS);
    if (json != null) {
      for (Object key : ImmutableList.copyOf(json.keys())) {
        String logType = (String) key;
        Level level = Level.parse((String) json.get(logType));
        ret.enable(logType, level);
      }
    }
    return ret;
  }

  public URL getAppURL() {
    String app = (String) getCapability("app");
    try {
      return app == null ? null : new URL(app);
    } catch (MalformedURLException e) {
      throw new WebDriverException(
          "The 'app' key is supposed to point to a URL." + app + " is not a URL.");
    }
  }

  public void setSDKVersion(String sdkVersion) {
    setCapability(UI_SDK_VERSION, sdkVersion);
  }
}
