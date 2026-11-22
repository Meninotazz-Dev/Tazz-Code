; INSTALADOR DO TAZZCODE (Inno Setup: jrsoftware.org, é grátis)
; Quem chama este arquivo é o Empacotar.bat. Ele passa a versão (/DVersao=0.7).
;
; Onde cada coisa fica depois de instalar:
;   C:\Program Files\TazzCode            o programa (este instalador coloca)
;   C:\ProgramData\TazzCode              plugins, mini IA, compilados (o instalador cria e libera pra todos)
;   AppData\Roaming\TazzCode             configurações de cada usuário (o TazzCode cria)
;   Documentos\TazzCode                   projetos de cada usuário (o TazzCode cria)

#ifndef Versao
  #define Versao "0.0"
#endif

[Setup]
AppId={{B3F2C6A1-7D4E-4C8B-9A61-2F5E8D3C1A70}
AppName=TazzCode
AppVersion={#Versao}
AppVerName=TazzCode {#Versao}
AppPublisher=João
DefaultDirName={autopf}\TazzCode
DefaultGroupName=TazzCode
DisableProgramGroupPage=yes
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
OutputDir=..\dist
OutputBaseFilename=Instalar-TazzCode-{#Versao}
SetupIconFile=..\dist\icone.ico
UninstallDisplayIcon={app}\TazzCode.exe
UninstallDisplayName=TazzCode
Compression=lzma2
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "pt"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "atalho"; Description: "Criar um atalho na Área de Trabalho"

[Dirs]
; A pasta das coisas grandes: todos os usuários do PC podem escrever nela
Name: "{commonappdata}\TazzCode"; Permissions: users-modify

[Files]
Source: "..\dist\app\TazzCode\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\TazzCode"; Filename: "{app}\TazzCode.exe"
Name: "{autodesktop}\TazzCode"; Filename: "{app}\TazzCode.exe"; Tasks: atalho

[Run]
Filename: "{app}\TazzCode.exe"; Description: "Abrir o TazzCode agora"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
; Ao desinstalar, apaga só o que dá pra criar de novo. Projetos e configurações NÃO são apagados.
Type: filesandordirs; Name: "{commonappdata}\TazzCode\compilados"
Type: filesandordirs; Name: "{commonappdata}\TazzCode\temp"
