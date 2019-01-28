// IRMPowerActionService.aidl
package com.ryuunoakaihitomi.rebootmenu;

interface IRMPowerActionService {
    void lockScreen();
    //Primitives are in by default, and cannot be otherwise.
    void reboot(in String reason);
    void safeMode();
    void shutdown();
    void hotReboot();
    void ping();
}
