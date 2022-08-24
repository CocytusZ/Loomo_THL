# Specifiction of Loomo project

The Loomo project has three part
1. An application deployed on Loomo robot, which is the android project in directory named *Loomo*
2. An host computer application which can deployed on android device(Given the UI layout, better the tablet). This is the project in directoru named *LoomoServer*
3. The Loomo application also include the function of receive position information from an external UWB position system. The UWB system is depoyed in the third floor of build 18.

## How to launch the project
The demo project is basically implement a remote control function. 

### UDP connection
The launch procedure is as followed:
1. Compile Loomo and LoomoServer project. Pay attention to gradle!
2. Install two project to Loomo and tablet seperately.
3. Use **your own hotspot** to compose a LAN, and connect Loomo and tablet to this LAN.
4. Open Loomo application, wait for all services initialized, and then press start button.
5. Open host application. Find the guide line menu on the top right corner. Click the first option, which looks like 控制面板.
6. Then a control panel will pop up. On the top right corner, switch the connection mode to UDP. Then input connection param of Loomo
    1. "InetAddress" is the ip of Loomo, you can get it from Loomo Wifi settings(Loomo is also an android device).
    2. The value of "port" is 1122. This is the default value, you can change it in Loomo's project.
    3. "Message" does not matters.
    4. Then press UDP_CONNECT button.
7. After connected, you can directly control Loomo through four buttons at the bottom right corner, which looks like "前", "后", "左" and "右".
8. You also can control Loomo through order. By which you need to input "distination" and "angle" field, and then press "START" button.

### Bluetooth connection 
There are other functions which may not stable. For instance, blue tooth connection. It is a substitution for UDP
To connected through bluetooth. You need to do as followed:
0. Open bluetooth of Loomo, and set Loomo as visiable.
1. Return to inital page of Host application
2. Click the third option in guideline menu, which looks like "查找设备".
3. A list of observable device will show up. Choose the one looks like Loomo and then match it. When I debugging, it is "gmin-bluetooth".
4. Then click the fourth option of guideline options, which looks like "已绑定". If you manage to connect, then a control panel will pop up. Keep the connection mode in "BT" and then you can do the same thing as when it connect through UDP.

### UWP position system
The Loomo application also contains a function of receiving position information from an external UWB system. To operate this system, please ask Mr. Schmidt in office 18-2.18 for help. As for the usage of UWB system, I will clarify in source code specification.

## Source code specification
In china, programmers always call these bad code as "a mountain of shit". Without doubt. you are facing a shit mountain now. My suggestion is to refactor all projects before doing further work. The following part will show you how to understand the project.

### Host program
