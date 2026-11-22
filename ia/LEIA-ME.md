# A IA do TazzHelper

Feita em Python, só com a biblioteca padrão (não precisa de `pip install`).

    servidor.py              o servidor (porta 7878) que o TazzCode usa
    instalar.py              baixa a mini IA (llama.cpp + Gemma 3 270M, ~280 MB)
    tazzhelper/
        conhecimento.py      O QUE O ESPECIALISTA SABE (perguntas e respostas, erros) — ensine coisas novas aqui!
        especialista.py      acha a melhor resposta (TF-IDF: compara as palavras da pergunta)
        analisador.py        explica código linha por linha e dá dicas
        mini_ia.py           conversa com a mini IA (llama-server, porta 7879)

Testar na mão:

    python servidor.py --pasta C:\Users\SeuNome\TazzCode\ia
    (abra no navegador: http://127.0.0.1:7878/saude)

Desafio: adicione um conceito novo em `conhecimento.py` (copie um que já existe e mude),
reinicie o TazzCode e pergunte pro TazzHelper!
