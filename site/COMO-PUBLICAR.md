# Como publicar o site do TazzCode (grátis)

O site é organizado assim:

    index.html        a página
    css/estilo.css    as cores, fontes e o layout
    js/site.js        os scripts
    img/logo.png      a logo
    downloads/        o instalador

## 1) Coloque o instalador
Rode o **Empacotar.bat**: ele copia o `Instalar-TazzCode-0.7.exe` pra `site\downloads` sozinho.

## 2) Publique no GitHub Pages (peça pra um adulto: o GitHub exige conta de alguém com 13 anos ou mais)
1. Crie uma conta em github.com e um repositório novo (ex.: `tazzcode`), público.
2. Clique em **Add file > Upload files** e arraste TUDO da pasta site (index.html e as pastas css, js, img e downloads).
3. Vá em **Settings > Pages**, em "Branch" escolha `main` e clique **Save**.
4. Em 1 ou 2 minutos o site aparece em `https://NOMEDACONTA.github.io/tazzcode`.

Limite do GitHub: cada arquivo pode ter até 100 MB (o instalador fica bem abaixo disso).
Pra arquivos maiores, use a aba **Releases** do repositório e troque o link do botão "Baixar".

## Versão nova?
Mude o número no `index.html` (procure por `0.7`), rode o Empacotar.bat e suba os arquivos de novo.
