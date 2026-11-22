#!/usr/bin/env bash
# =====================================================================
#  EMPACOTAR O TAZZCODE PRO LINUX (todos os formatos de uma vez)
#
#  Cria, com o Java dentro:
#    tazzcode_VERSAO_amd64.deb          Ubuntu, Debian, Mint, Kali, Pop!_OS, Zorin, Winux...
#    tazzcode-VERSAO-1.x86_64.rpm       Fedora, openSUSE...
#    tazzcode-VERSAO-1-x86_64.pkg.tar.zst   Arch, Manjaro...
#    tazzcode-VERSAO-linux-x64.tar.gz   qualquer Linux (sem instalar nada no sistema)
#
#  Rodar:  bash empacotar-linux.sh
#  Precisa: JDK 21+ (sudo apt install openjdk-21-jdk)
#  Opcional (pra cada formato): dpkg-deb (já vem no Ubuntu), rpmbuild (sudo apt install rpm),
#                               bsdtar + zstd (sudo apt install libarchive-tools zstd)
#  Se faltar a ferramenta de um formato, ele pula só aquele.
# =====================================================================
cd "$(dirname "$0")"
set -e

bash _compilar.sh
JAVA_BIN="$(dirname "$(readlink -f "$(command -v javac || echo "$JAVA_HOME/bin/javac")")")"
VERSAO="$(grep -o 'VERSAO = "[^"]*"' src/tazzcode/App.java | cut -d'"' -f2)"
DESC="Editor de codigo feito em Java, com 114 linguagens, console e a IA TazzHelper"
SAIDA="dist-linux"
echo "Versão: $VERSAO"

rm -rf "$SAIDA" && mkdir -p "$SAIDA/entrada"

# ---------- 1) o programa com o Java dentro (app-image) ----------
echo "[1/5] Montando o programa com o Java dentro..."
"$JAVA_BIN/jar" --create --file "$SAIDA/entrada/TazzCode.jar" --main-class tazzcode.Main -C out .
cp -r ia "$SAIDA/entrada/ia"
find "$SAIDA/entrada/ia" -name __pycache__ -type d -prune -exec rm -rf {} +
"$JAVA_BIN/jpackage" --type app-image --name tazzcode --app-version "$VERSAO" \
    --input "$SAIDA/entrada" --main-jar TazzCode.jar --main-class tazzcode.Main \
    --icon recursos/icone.png --dest "$SAIDA/app" --vendor "Meninotazz Dev" \
    --java-options "-Dfile.encoding=UTF-8" \
    --add-modules java.base,java.desktop,java.net.http,jdk.httpserver,jdk.charsets,jdk.localedata \
    --jlink-options "--strip-debug --no-header-files --no-man-pages --include-locales=pt-BR,en"
APP="$SAIDA/app/tazzcode"

# ---------- a "raiz" que todos os pacotes instalam ----------
RAIZ="$SAIDA/raiz"
mkdir -p "$RAIZ/opt" "$RAIZ/usr/bin" "$RAIZ/usr/share/applications" "$RAIZ/usr/share/icons/hicolor/256x256/apps"
cp -r "$APP" "$RAIZ/opt/tazzcode"
ln -s /opt/tazzcode/bin/tazzcode "$RAIZ/usr/bin/tazzcode"
cp recursos/icone.png "$RAIZ/usr/share/icons/hicolor/256x256/apps/tazzcode.png"
cat > "$RAIZ/usr/share/applications/tazzcode.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=TazzCode
GenericName=Editor de código
Comment=$DESC
Exec=/opt/tazzcode/bin/tazzcode %F
Icon=tazzcode
Terminal=false
Categories=Development;IDE;TextEditor;
Keywords=java;python;codigo;programar;editor;
StartupWMClass=tazzcode-Main
EOF
TAMANHO_KB=$(du -sk "$RAIZ" | cut -f1)

# ---------- 2) .deb ----------
if command -v dpkg-deb >/dev/null; then
    echo "[2/5] Criando o .deb..."
    D="$SAIDA/deb"
    rm -rf "$D" && cp -a "$RAIZ" "$D" && mkdir -p "$D/DEBIAN"
    cat > "$D/DEBIAN/control" <<EOF
Package: tazzcode
Version: $VERSAO
Architecture: amd64
Maintainer: Meninotazz Dev <meninotazzdev.org@gmail.com>
Installed-Size: $TAMANHO_KB
Section: devel
Priority: optional
Depends: libc6 (>= 2.38), libasound2t64 | libasound2, libfreetype6, libgcc-s1, libgif7, libharfbuzz0b, liblcms2-2, libpng16-16t64 | libpng16-16, libstdc++6, libx11-6, libxext6, libxi6, libxrender1, libxtst6, zlib1g
Recommends: python3, xdg-utils
Homepage: https://github.com/Meninotazz-Dev/
Description: $DESC
 TazzCode roda 114 linguagens, tem console, plugins e o TazzHelper,
 uma IA que roda no proprio computador. Ja vem com o Java dentro.
EOF
    dpkg-deb --root-owner-group --build "$D" "$SAIDA/tazzcode_${VERSAO}_amd64.deb" >/dev/null
else
    echo "[2/5] (sem dpkg-deb: pulei o .deb)"
fi

# ---------- 3) .rpm ----------
if command -v rpmbuild >/dev/null; then
    echo "[3/5] Criando o .rpm..."
    R="$(readlink -f "$SAIDA")/rpm"
    rm -rf "$R" && mkdir -p "$R"/{BUILD,RPMS,SOURCES,SPECS,SRPMS,BUILDROOT}
    cat > "$R/SPECS/tazzcode.spec" <<EOF
Name:           tazzcode
Version:        $VERSAO
Release:        1
Summary:        $DESC
License:        GPL-3.0-or-later
URL:            https://github.com/Meninotazz-Dev/
BuildArch:      x86_64
AutoReqProv:    no
Requires:       libasound.so.2()(64bit), libfreetype.so.6()(64bit), libgif.so.7()(64bit), libharfbuzz.so.0()(64bit)
Requires:       liblcms2.so.2()(64bit), libpng16.so.16()(64bit), libX11.so.6()(64bit), libXext.so.6()(64bit)
Requires:       libXi.so.6()(64bit), libXrender.so.1()(64bit), libXtst.so.6()(64bit), libz.so.1()(64bit)
Recommends:     python3

%description
TazzCode roda 114 linguagens, tem console, plugins e o TazzHelper,
uma IA que roda no proprio computador. Ja vem com o Java dentro.

%install
mkdir -p %{buildroot}
cp -a $(readlink -f "$RAIZ")/. %{buildroot}/

%files
/opt/tazzcode
/usr/bin/tazzcode
/usr/share/applications/tazzcode.desktop
/usr/share/icons/hicolor/256x256/apps/tazzcode.png
EOF
    rpmbuild --quiet --define "_topdir $R" --define "_build_id_links none" --define "__strip /bin/true" \
        --define "__brp_strip /bin/true" --define "__os_install_post %{nil}" -bb "$R/SPECS/tazzcode.spec"
    cp "$R"/RPMS/x86_64/*.rpm "$SAIDA/"
else
    echo "[3/5] (sem rpmbuild: pulei o .rpm)"
fi

# ---------- 4) Arch (.pkg.tar.zst) ----------
if command -v bsdtar >/dev/null && command -v zstd >/dev/null; then
    echo "[4/5] Criando o pacote do Arch..."
    A="$SAIDA/arch"
    rm -rf "$A" && cp -a "$RAIZ" "$A"
    cat > "$A/.PKGINFO" <<EOF
pkgname = tazzcode
pkgbase = tazzcode
pkgver = ${VERSAO}-1
pkgdesc = $DESC
url = https://github.com/Meninotazz-Dev/
builddate = $(date +%s)
packager = Meninotazz Dev <meninotazzdev.org@gmail.com>
size = $((TAMANHO_KB * 1024))
arch = x86_64
license = GPL-3.0-or-later
depend = glibc
depend = gcc-libs
depend = alsa-lib
depend = freetype2
depend = giflib
depend = harfbuzz
depend = lcms2
depend = libpng
depend = libx11
depend = libxext
depend = libxi
depend = libxrender
depend = libxtst
depend = zlib
optdepend = python: a IA do TazzHelper
EOF
    ( cd "$A" && LANG=C bsdtar -czf .MTREE --format=mtree \
        --options='!all,use-set,type,uid,gid,mode,time,size,md5,sha256,link' .PKGINFO opt usr \
      && LANG=C bsdtar -cf - .MTREE .PKGINFO opt usr | zstd -q -19 -T0 -o "../tazzcode-${VERSAO}-1-x86_64.pkg.tar.zst" )
else
    echo "[4/5] (sem bsdtar/zstd: pulei o pacote do Arch)"
fi

# ---------- 5) .tar.gz (qualquer Linux) ----------
echo "[5/5] Criando o .tar.gz..."
T="$SAIDA/tar/tazzcode-$VERSAO"
rm -rf "$SAIDA/tar" && mkdir -p "$T"
cp -a "$APP"/. "$T/"
cp recursos/icone.png "$T/tazzcode.png"
cat > "$T/instalar.sh" <<'EOF'
#!/usr/bin/env bash
# Instala o TazzCode SÓ PRO SEU USUÁRIO (sem sudo): ~/.local/share/tazzcode
set -e
AQUI="$(cd "$(dirname "$0")" && pwd)"
DESTINO="$HOME/.local/share/tazzcode-app"
rm -rf "$DESTINO" && mkdir -p "$DESTINO" && cp -a "$AQUI"/. "$DESTINO/"
mkdir -p "$HOME/.local/bin" "$HOME/.local/share/applications"
ln -sf "$DESTINO/bin/tazzcode" "$HOME/.local/bin/tazzcode"
cat > "$HOME/.local/share/applications/tazzcode.desktop" <<FIM
[Desktop Entry]
Type=Application
Name=TazzCode
Comment=Editor de código
Exec=$DESTINO/bin/tazzcode %F
Icon=$DESTINO/tazzcode.png
Terminal=false
Categories=Development;IDE;TextEditor;
StartupWMClass=tazzcode-Main
FIM
echo "Pronto! O TazzCode está no menu de aplicativos (ou rode: ~/.local/bin/tazzcode)."
EOF
cat > "$T/desinstalar.sh" <<'EOF'
#!/usr/bin/env bash
rm -rf "$HOME/.local/share/tazzcode-app" "$HOME/.local/bin/tazzcode" "$HOME/.local/share/applications/tazzcode.desktop"
echo "TazzCode removido (seus projetos continuam em Documentos/TazzCode)."
EOF
chmod +x "$T/instalar.sh" "$T/desinstalar.sh"
tar -C "$SAIDA/tar" -czf "$SAIDA/tazzcode-${VERSAO}-linux-x64.tar.gz" "tazzcode-$VERSAO"

# ---------- pronto ----------
mkdir -p site/downloads
cp "$SAIDA"/tazzcode*.{deb,rpm,zst,gz} site/downloads/ 2>/dev/null || true
echo
echo "============================================================"
echo "  PRONTO! Os pacotes ficaram em $SAIDA/:"
ls -1sh "$SAIDA" | grep -E "\.(deb|rpm|zst|gz)$" | sed 's/^/    /'
echo
echo "  Ubuntu/Debian/Mint/Kali/Pop!_OS/Zorin/Winux: sudo apt install ./tazzcode_${VERSAO}_amd64.deb"
echo "  Fedora:   sudo dnf install ./tazzcode-${VERSAO}-1.x86_64.rpm"
echo "  openSUSE: sudo zypper install ./tazzcode-${VERSAO}-1.x86_64.rpm"
echo "  Arch/Manjaro: sudo pacman -U tazzcode-${VERSAO}-1-x86_64.pkg.tar.zst"
echo "  Qualquer um: tar xzf tazzcode-${VERSAO}-linux-x64.tar.gz && ./tazzcode-${VERSAO}/instalar.sh"
echo "============================================================"
