; -- Setup_Full_Template.iss --
; installer date 010315A

[Setup]
WizardStyle=modern
DisableStartupPrompt=true
UninstallStyle=modern
AppPublisher=iZotope, Inc.
AppPublisherURL=http://www.izotope.com
AppVersion=1.00
AppName=iZotope Automation Toolbox
AppVerName=iZotope Automation Toolbox
AppCopyright=Copyright © 2013 iZotope, Inc.
DefaultDirName={sd}\AutomationToolbox
DefaultGroupName=iZotope\Automation_Toolbox
Uninstallable=true
OutputBaseFilename=AutomationToolbox_Setup
ShowLanguageDialog=no
BackColor=clBlack
BackColor2=clBlack
WizardImageBackColor=clWhite
AppSupportURL=http://www.izotope.com
AppUpdatesURL=http://www.izotope.com
Compression=zip/9
UsePreviousSetupType=no
MinVersion=4.1.1998,5.0.2195
AppMutex=iZotope_Automation_Toolbox_Mutex
; We require administrator access since we change the system path under Win2K/XP
PrivilegesRequired=admin
RestartIfNeededByRun=no

; Desktop shortcuts and quicklaunch options
[Tasks] 

[Types]
Name: "full"; Description: "Full Installation"

[Components]

[Files]
Source: D:\Repositories\TestAutomation\C++\RPCServer\Binaries\32\iZTestAutomation.dll;  DestDir: {sys}; CopyMode: alwaysoverwrite; Flags: replacesameversion;
Source: D:\Repositories\TestAutomation\C++\RPCServer\Binaries\32\iZTestAutomationD.dll; DestDir: {sys}; CopyMode: alwaysoverwrite; Flags: replacesameversion;
Source: D:\Repositories\TestAutomation\C++\RPCServer\Binaries\64\iZTestAutomation.dll;  DestDir: {sys}; CopyMode: alwaysoverwrite; Flags: replacesameversion; DestName: iZTestAutomation64.dll
Source: D:\Repositories\TestAutomation\C++\RPCServer\Binaries\64\iZTestAutomationD.dll; DestDir: {sys}; CopyMode: alwaysoverwrite; Flags: replacesameversion; DestName: iZTestAutomationD64.dll

Source: D:\Repositories\TestAutomation\Java\AutomationToolbox.jar;        DestDir: {app};               CopyMode: alwaysoverwrite; Flags: replacesameversion;
Source: D:\Repositories\TestAutomation\Java\AutomationToolbox.bat;        DestDir: {app};               CopyMode: alwaysoverwrite; Flags: replacesameversion;
Source: D:\Repositories\TestAutomation\Java\AutomationToolbox\Preferences\Prefs.xml; DestDir: {app}\AutomationToolbox\Preferences;  CopyMode: alwaysoverwrite; Flags: replacesameversion;
Source: D:\Repositories\TestAutomation\Java\Jars\*.*;                     DestDir: {app}\Jars;          CopyMode: alwaysoverwrite; Flags: replacesameversion recursesubdirs createallsubdirs;
Source: D:\Repositories\TestAutomation\Installers\JRE\jre7\*.*;           DestDir: {app}\JRE\jre7;      CopyMode: alwaysoverwrite; Flags: replacesameversion recursesubdirs createallsubdirs; 
Source: D:\Repositories\TestAutomation\Installers\MIDI\MidiYokeSetup.msi; DestDir: {app}\MIDI;          CopyMode: alwaysoverwrite; Flags: replacesameversion;

[Dirs]

[Icons]

[Registry]

[_ISTool]
EnableISX=true

[Run]

; Launch our application if desired
Filename: "{app}\AutomationToolbox.bat"; Description: "Launch AutomationToolbox"; Flags: postinstall nowait skipifsilent
; Some Clean up
Filename: "rm"; Description: ""; Parameters: "{app}\Jars\sigar\libsigar-universal64-macosx.dylib";

[UninstallRun]

[UninstallDelete]

[InstallDelete]

[Code]
