// IRMPowerActionService.aidl
package com.ryuunoakaihitomi.rebootmenu;

interface IRMPowerActionService {
    void lockScreen();
    void reboot(String reason);
    void safeMode();
    void shutdown();
    void hotReboot();
    void ping();
}
