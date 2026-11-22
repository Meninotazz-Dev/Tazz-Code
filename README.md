## ✨ O que tem de novo

- 🤖 **TazzHelper com IA própria**: feita em Python, roda no seu PC, sem internet e sem chave.
  Explica erros, conceitos e o seu código linha por linha. A **mini IA** (Gemma 3 270M) é opcional.
- 🎨 **Visual novo**: botões arredondados com ícones, abas modernas e 4 temas:
  **Meia-noite**, **Grafite**, **Oceano** e **Claro**.
- ⚙️ **Configurações** (Ctrl+vírgula): fonte e tamanho da interface e do código, tamanho do TAB,
  fechar parênteses sozinho, sugestões, quebra de linha, salvar sozinho e mais.
- 🐧 **12 distribuições Linux**: .deb, .rpm, Arch e .tar.gz. O tutorial mostra o comando certo
  (apt, dnf, zypper ou pacman) pra instalar cada linguagem.
- 📦 **Instalador pro Windows**: instala no Program Files e cria atalho.
- ☕ **Todos os pacotes já trazem o Java dentro.**

## 🐛 Arrumados

- O C/C++ (GCC) não funcionava quando o nome do usuário tinha acento ou espaço.
- O TazzCode travava por minutos quando um programa escrevia muito no console.
- `public class Main` virava o atalho `main` sozinho.

## 📥 Qual baixar?

| Sistema | Arquivo | Como instalar |
|---|---|---|
| **Windows** 10 e 11 | `Instalar-TazzCode-0.7.exe` | Dois cliques |
| **Ubuntu, Debian, Mint, Kali, Pop!_OS, Zorin OS, Winux, elementary OS** | `tazzcode_0.7_amd64.deb` | `sudo apt install ./tazzcode_0.7_amd64.deb` |
| **Fedora** | `tazzcode-0.7-1.x86_64.rpm` | `sudo dnf install ./tazzcode-0.7-1.x86_64.rpm` |
| **openSUSE** | `tazzcode-0.7-1.x86_64.rpm` | `sudo zypper install ./tazzcode-0.7-1.x86_64.rpm` |
| **Arch, Manjaro, EndeavourOS** | `tazzcode-0.7-1-x86_64.pkg.tar.zst` | `sudo pacman -U tazzcode-0.7-1-x86_64.pkg.tar.zst` |
| **Qualquer outro Linux** | `tazzcode-0.7-linux-x64.tar.gz` | `tar xzf tazzcode-0.7-linux-x64.tar.gz && ./tazzcode-0.7/instalar.sh` |

**Linux:** precisa de uma versão de 2024 pra frente (ex.: Ubuntu 24.04, Debian 13, Mint 22, Fedora 40).

**Windows:** apareceu "O Windows protegeu o computador"? É normal em programa novo sem assinatura digital.
Clique em **Mais informações** e depois em **Executar assim mesmo**.

**Conferir os arquivos:** baixe o `SHA256SUMS.txt` pra mesma pasta e rode `sha256sum -c SHA256SUMS.txt --ignore-missing`.
