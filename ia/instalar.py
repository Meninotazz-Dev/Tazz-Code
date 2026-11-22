"""
INSTALADOR DA MINI IA do TazzHelper.

Baixa duas coisas pra pasta escolhida:
  1) o llama.cpp (o "motor" que faz a IA pensar)        ~ 20-40 MB
  2) a Gemma 3 270M (o "cérebro", já treinado)          ~ 250 MB

Rodar:  python instalar.py --pasta C:\\caminho\\da\\ia
"""
import argparse
import json
import os
import shutil
import sys
import urllib.request
import platform
import stat
import tarfile
import zipfile

MODELO_URL = "https://huggingface.co/unsloth/gemma-3-270m-it-GGUF/resolve/main/gemma-3-270m-it-Q4_K_M.gguf"
MODELO_NOME = "gemma-3-270m-it-Q4_K_M.gguf"
LLAMA_REPO = "https://api.github.com/repos/ggml-org/llama.cpp/releases?per_page=15"


def baixar(url, destino, nome):
    req = urllib.request.Request(url, headers={"User-Agent": "TazzCode"})
    with urllib.request.urlopen(req, timeout=60) as r, open(destino + ".parte", "wb") as f:
        total = int(r.headers.get("Content-Length") or 0)
        feito, ultimo = 0, -1
        while True:
            bloco = r.read(1 << 16)
            if not bloco:
                break
            f.write(bloco)
            feito += len(bloco)
            if total:
                pct = feito * 100 // total
                if pct != ultimo and pct % 5 == 0:
                    print(f"PROGRESSO {pct} Baixando {nome}: {feito / 1048576:.0f} de {total / 1048576:.0f} MB", flush=True)
                    ultimo = pct
    os.replace(destino + ".parte", destino)


def link_do_llama():
    """Acha o .zip do llama.cpp pra Windows (só CPU, funciona em qualquer PC)."""
    req = urllib.request.Request(LLAMA_REPO, headers={"User-Agent": "TazzCode"})
    with urllib.request.urlopen(req, timeout=30) as r:
        lancamentos = json.loads(r.read().decode("utf-8"))
    # Windows: "...-bin-win-cpu-x64.zip"   Linux (Ubuntu): "...-bin-ubuntu-x64.tar.gz" (só CPU: roda em qualquer PC)
    marca = "-bin-win-cpu-x64." if os.name == "nt" else "-bin-ubuntu-x64."
    for rel in lancamentos:   # o mais novo que tiver a versão pro sistema
        for a in rel.get("assets", []):
            nome = a.get("name", "")
            if marca in nome and (nome.endswith(".zip") or nome.endswith(".tar.gz")):
                return a["browser_download_url"], nome
    raise RuntimeError("não achei o llama.cpp pra " + platform.system() + " no GitHub")


def necessario(nome):
    """Só o que a IA precisa: o llama-server e as bibliotecas (.dll no Windows, .so no Linux)."""
    return nome in ("llama-server", "llama-server.exe") or nome.lower().endswith(".dll") or ".so" in nome


def extrair_o_necessario(arquivo, destino):
    if arquivo.endswith(".zip"):
        with zipfile.ZipFile(arquivo) as z:
            for info in z.infolist():
                nome = os.path.basename(info.filename)
                if nome and necessario(nome):
                    with z.open(info) as origem, open(os.path.join(destino, nome), "wb") as dest:
                        shutil.copyfileobj(origem, dest)
    else:
        with tarfile.open(arquivo) as t:
            links = []
            for m in t.getmembers():
                nome = os.path.basename(m.name)
                if not nome or not necessario(nome):
                    continue
                if m.issym() or m.islnk():   # libllama.so -> libllama.so.0 (refaz o atalho depois)
                    links.append((nome, os.path.basename(m.linkname)))
                elif m.isfile():
                    with t.extractfile(m) as origem, open(os.path.join(destino, nome), "wb") as dest:
                        shutil.copyfileobj(origem, dest)
            for nome, alvo in links:
                caminho = os.path.join(destino, nome)
                if not os.path.exists(caminho) and os.path.exists(os.path.join(destino, alvo)):
                    os.symlink(alvo, caminho)
        servidor = os.path.join(destino, "llama-server")
        if os.path.isfile(servidor):
            os.chmod(servidor, os.stat(servidor).st_mode | stat.S_IEXEC | stat.S_IXGRP | stat.S_IXOTH)


def main():
    args = argparse.ArgumentParser()
    args.add_argument("--pasta", required=True)
    pasta = args.parse_args().pasta
    os.makedirs(os.path.join(pasta, "modelo"), exist_ok=True)

    # 1) llama.cpp
    print("PROGRESSO 0 Procurando o llama.cpp mais novo...", flush=True)
    url, nome = link_do_llama()
    zip_destino = os.path.join(pasta, nome)
    baixar(url, zip_destino, "llama.cpp")
    print("PROGRESSO 100 Extraindo o llama.cpp...", flush=True)
    llama = os.path.join(pasta, "llama")
    shutil.rmtree(llama, ignore_errors=True)
    os.makedirs(llama)
    extrair_o_necessario(zip_destino, llama)
    os.remove(zip_destino)

    # 2) o modelo (cérebro)
    destino = os.path.join(pasta, "modelo", MODELO_NOME)
    if not os.path.isfile(destino):
        baixar(MODELO_URL, destino, "o cérebro (Gemma 3 270M)")

    total = 0
    for raiz, _, arqs in os.walk(pasta):
        for a in arqs:
            total += os.path.getsize(os.path.join(raiz, a))
    with open(os.path.join(pasta, "instalado.json"), "w", encoding="utf-8") as f:
        json.dump({"modelo": MODELO_NOME, "llama": nome, "tamanho_mb": round(total / 1048576)}, f)
    print(f"PRONTO Mini IA instalada! Tamanho total: {total / 1048576:.0f} MB", flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:   # noqa: BLE001
        print(f"ERRO {e}", flush=True)
        sys.exit(1)
