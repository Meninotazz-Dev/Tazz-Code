<p align="center">
  <img src="recursos/icone.png" width="128" alt="Logo do TazzCode">
</p>

<h1 align="center">TazzCode</h1>

<p align="center">
  Um editor de código feito do zero, em Java, por um programador que está aprendendo.<br>
  114 linguagens, console, plugins e o <b>TazzHelper</b>, uma IA que roda no seu computador.
</p>

---

## ✨ O que ele faz

- **Windows e Linux**: instalador pro Windows e pacotes pra 12 distribuições Linux.
- **114 linguagens**: escreva e aperte **F5** pra compilar e rodar (Java, Python, C, C++, JavaScript, Go, Rust...).
- **Console de verdade**: dá pra digitar no programa enquanto ele roda.
- **Plugins**: não tem a linguagem no PC? Um clique baixa uma versão portátil, direto do site oficial dela.
- **TazzHelper**: uma IA própria, em Python, que explica os erros, os conceitos e o seu código linha por linha.
  Tem o **modo professor**, que dá dicas em vez de respostas prontas.
- **Banco de dados de mentirinha**: SQL, MongoDB e Redis abrem uma janela que simula o banco.
- **HTML que avisa o erro**: tag errada ganha uma ondinha vermelha e conserto rápido (**Alt+Enter**).
- **83 modelos de projeto**: jogo em Java, site, Flask, React e muito mais.
- **Configurações**: 4 temas (Meia-noite, Grafite, Oceano e Claro), fontes, tamanho do TAB e muito mais.

## 📥 Baixar

Os instaladores ficam na aba **[Releases](../../releases)**:

| Sistema | Arquivo |
|---|---|
| **Windows** 10 e 11 (64 bits) | `Instalar-TazzCode-0.7.exe` |
| **Ubuntu, Debian, Linux Mint, Kali, Pop!_OS, Zorin OS, Winux, elementary OS** | `tazzcode_0.7_amd64.deb` |
| **Fedora, openSUSE** | `tazzcode-0.7-1.x86_64.rpm` |
| **Arch Linux, Manjaro, EndeavourOS** | `tazzcode-0.7-1-x86_64.pkg.tar.zst` |
| **Qualquer outro Linux** | `tazzcode-0.7-linux-x64.tar.gz` |

Todos já trazem o Java dentro. No Linux, precisa de uma versão de 2024 pra frente
(ex.: Ubuntu 24.04, Debian 13, Mint 22, Fedora 40).

## 🛠️ Rodar pelo código

Precisa do **JDK 21 ou mais novo**.

| | Windows | Linux |
|---|---|---|
| Compilar e abrir | `Rodar.bat` | `bash rodar.sh` |
| Ver os erros, se não abrir | `Rodar-VerErros.bat` | (aparecem no terminal) |
| Criar os instaladores | `Empacotar.bat` (+ [Inno Setup](https://jrsoftware.org)) | `bash empacotar-linux.sh` (cria .deb, .rpm, Arch e .tar.gz) |

## 📁 Pastas

| Pasta | O que tem |
|---|---|
| `src/tazzcode/` | todo o código Java do editor |
| `ia/` | o TazzHelper (Python, sem precisar de `pip install`) |
| `recursos/` | o ícone |
| `instalador/` | o script do instalador do Windows |
| `exemplos/` | arquivos de exemplo |
| `site/` | o site de download |

O manual completo, com todas as novidades de cada versão, está no **[LEIA-ME.md](LEIA-ME.md)**.

---

Licença: **GNU GPL v3.0** (o código do TazzCode). O Java que vai junto nos instaladores, a mini IA Gemma
e o llama.cpp continuam com as licenças deles.

<p align="center">Feito por <b>Meninotazz Dev</b> · Programando e Continuando!</p>
