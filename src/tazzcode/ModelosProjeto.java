package tazzcode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODELOS DE PROJETO: os frameworks e bibliotecas do OneCompiler (React, Flask, Spring Boot,
 * Pygame...) viram projetos prontos no "Novo projeto".
 *
 * Cada modelo tem:
 *   arquivos -> os arquivos que ele cria
 *   instalar -> um comando que roda UMA vez quando o projeto é criado (ex.: pip install flask)
 *   rodar    -> o comando do F5 (fica salvo no arquivo .tazzcode do projeto)
 *   url      -> se for um servidor, o endereço que abre no navegador
 *
 * As páginas web usam bibliotecas pela internet (CDN), então não precisa instalar nada,
 * mas precisa de internet pra funcionar. Elas abrem por um servidorzinho do próprio
 * TazzCode (ServidorLocal), porque várias bibliotecas não funcionam abrindo o arquivo direto.
 */
public class ModelosProjeto {

    public static class Modelo {
        public final String nome, categoria;
        public final Map<String, String> arquivos = new LinkedHashMap<>();
        public String abrir = "";      // arquivo que abre no editor
        public String rodar = "";      // comando do F5 ("navegador:index.html" abre no navegador)
        public String instalar = "";   // roda uma vez ao criar
        public String url = "";
        public String precisa = "";

        Modelo(String categoria, String nome) {
            this.categoria = categoria;
            this.nome = nome;
        }

        Modelo arquivo(String caminho, String conteudo) {
            arquivos.put(caminho, conteudo);
            if (abrir.isEmpty()) abrir = caminho;
            return this;
        }

        Modelo abrir(String a) { abrir = a; return this; }
        Modelo rodar(String r) { rodar = r; return this; }
        Modelo instalar(String i) { instalar = i; return this; }
        Modelo url(String u) { url = u; return this; }
        Modelo precisa(String p) { precisa = p; return this; }

        @Override public String toString() { return categoria + "  ›  " + nome; }
    }

    private static final List<Modelo> TODOS = new ArrayList<>();

    public static List<Modelo> todos() {
        if (TODOS.isEmpty()) cadastrar();
        return TODOS;
    }

    private static Modelo m(String cat, String nome) {
        Modelo x = new Modelo(cat, nome);
        TODOS.add(x);
        return x;
    }

    // =====================================================================

    private static final String PY = "Python";
    private static final String NODE = "Node.js";
    private static final String JAVA = "Java";
    private static final String GO = "Go";
    private static final String OUTROS = "Outras linguagens";
    private static final String WEB = "Web (abre no navegador)";
    private static final String REACT = "Web com React";
    private static final String GRANDES = "Web (projetos grandes, com npm)";

    private static final String PRECISA_PY = "Python: https://www.python.org/downloads";
    private static final String PRECISA_NODE = "Node.js: https://nodejs.org";
    private static final String PRECISA_MVN = "JDK + Maven: https://maven.apache.org/download.cgi";

    private static void cadastrar() {

        // ===================== PYTHON =====================
        m(PY, "Flask (site)").arquivo("app.py", "from flask import Flask\n\napp = Flask(__name__)\n\n\n@app.route(\"/\")\ndef inicio():\n    return \"<h1>Olá do Flask!</h1>\"\n\n\nif __name__ == \"__main__\":\n    app.run(debug=True)\n")
            .instalar("python -m pip install flask").rodar("python app.py").url("http://127.0.0.1:5000").precisa(PRECISA_PY);
        m(PY, "FastAPI (API)").arquivo("main.py", "from fastapi import FastAPI\n\napp = FastAPI()\n\n\n@app.get(\"/\")\ndef inicio():\n    return {\"mensagem\": \"Olá do FastAPI!\"}\n\n\n@app.get(\"/soma/{a}/{b}\")\ndef soma(a: int, b: int):\n    return {\"resultado\": a + b}\n")
            .instalar("python -m pip install fastapi uvicorn").rodar("python -m uvicorn main:app --reload").url("http://127.0.0.1:8000/docs").precisa(PRECISA_PY);
        m(PY, "Django (site grande)").arquivo("LEIA-ME.txt", "Projeto Django.\nO TazzCode instala o Django e cria o projeto na primeira vez.\nDepois aperte F5 e abra http://127.0.0.1:8000\n")
            .instalar("python -m pip install django && python -m django startproject site .").rodar("python manage.py runserver").url("http://127.0.0.1:8000").precisa(PRECISA_PY);
        m(PY, "Streamlit (app de dados)").arquivo("app.py", "import streamlit as st\n\nst.title(\"Olá do Streamlit!\")\nnome = st.text_input(\"Seu nome\")\nif nome:\n    st.write(f\"Prazer, {nome}!\")\nidade = st.slider(\"Idade\", 0, 100, 12)\nst.write(\"Daqui 10 anos você terá\", idade + 10)\n")
            .instalar("python -m pip install streamlit").rodar("python -m streamlit run app.py").precisa(PRECISA_PY);
        m(PY, "Gradio (app com IA/interface)").arquivo("app.py", "import gradio as gr\n\n\ndef ola(nome):\n    return f\"Olá, {nome}!\"\n\n\ngr.Interface(fn=ola, inputs=\"text\", outputs=\"text\").launch()\n")
            .instalar("python -m pip install gradio").rodar("python app.py").url("http://127.0.0.1:7860").precisa(PRECISA_PY);
        m(PY, "Bottle (site mini)").arquivo("app.py", "from bottle import route, run\n\n\n@route(\"/\")\ndef inicio():\n    return \"<h1>Olá do Bottle!</h1>\"\n\n\nrun(host=\"localhost\", port=8080)\n")
            .instalar("python -m pip install bottle").rodar("python app.py").url("http://localhost:8080").precisa(PRECISA_PY);
        m(PY, "Tornado (servidor)").arquivo("app.py", "import asyncio\nimport tornado.web\n\n\nclass Inicio(tornado.web.RequestHandler):\n    def get(self):\n        self.write(\"<h1>Olá do Tornado!</h1>\")\n\n\nasync def main():\n    app = tornado.web.Application([(r\"/\", Inicio)])\n    app.listen(8888)\n    await asyncio.Event().wait()\n\n\nasyncio.run(main())\n")
            .instalar("python -m pip install tornado").rodar("python app.py").url("http://localhost:8888").precisa(PRECISA_PY);
        m(PY, "Tkinter (janela)").arquivo("janela.py", "import tkinter as tk\n\njanela = tk.Tk()\njanela.title(\"Minha janela\")\njanela.geometry(\"300x150\")\n\nrotulo = tk.Label(janela, text=\"Olá do Tkinter!\", font=(\"Arial\", 16))\nrotulo.pack(pady=20)\n\n\ndef clicou():\n    rotulo.config(text=\"Você clicou!\")\n\n\ntk.Button(janela, text=\"Clique aqui\", command=clicou).pack()\njanela.mainloop()\n")
            .rodar("python janela.py").precisa(PRECISA_PY);
        m(PY, "Turtle (tartaruga que desenha)").arquivo("desenho.py", "import turtle\n\nt = turtle.Turtle()\nt.speed(0)\ncores = [\"blue\", \"deepskyblue\", \"white\"]\nturtle.bgcolor(\"black\")\nfor i in range(120):\n    t.pencolor(cores[i % 3])\n    t.forward(i * 2)\n    t.left(59)\nturtle.done()\n")
            .rodar("python desenho.py").precisa(PRECISA_PY);
        m(PY, "Pygame (jogo)").arquivo("jogo.py", "import pygame\n\npygame.init()\ntela = pygame.display.set_mode((640, 480))\npygame.display.set_caption(\"Meu jogo\")\nrelogio = pygame.time.Clock()\nx, y = 320, 240\n\nrodando = True\nwhile rodando:\n    for evento in pygame.event.get():\n        if evento.type == pygame.QUIT:\n            rodando = False\n    teclas = pygame.key.get_pressed()\n    if teclas[pygame.K_LEFT]: x -= 5\n    if teclas[pygame.K_RIGHT]: x += 5\n    if teclas[pygame.K_UP]: y -= 5\n    if teclas[pygame.K_DOWN]: y += 5\n    tela.fill((27, 34, 51))\n    pygame.draw.rect(tela, (47, 125, 246), (x - 20, y - 20, 40, 40))\n    pygame.display.flip()\n    relogio.tick(60)\n\npygame.quit()\n")
            .instalar("python -m pip install pygame").rodar("python jogo.py").precisa(PRECISA_PY);
        m(PY, "Matplotlib (gráfico)").arquivo("grafico.py", "import matplotlib.pyplot as plt\n\nmeses = [\"Jan\", \"Fev\", \"Mar\", \"Abr\"]\nmoedas = [10, 25, 18, 40]\nplt.plot(meses, moedas, marker=\"o\")\nplt.title(\"Minhas moedas\")\nplt.show()\n")
            .instalar("python -m pip install matplotlib").rodar("python grafico.py").precisa(PRECISA_PY);
        m(PY, "Seaborn (gráfico bonito)").arquivo("grafico.py", "import seaborn as sns\nimport matplotlib.pyplot as plt\n\ndados = sns.load_dataset(\"tips\")\nsns.barplot(data=dados, x=\"day\", y=\"total_bill\")\nplt.title(\"Conta média por dia\")\nplt.show()\n")
            .instalar("python -m pip install seaborn matplotlib").rodar("python grafico.py").precisa(PRECISA_PY + " (o exemplo baixa dados da internet)");

        // ===================== NODE.JS =====================
        String pkg = "{\n  \"name\": \"meu-projeto\",\n  \"version\": \"1.0.0\",\n  \"type\": \"module\"\n}\n";
        m(NODE, "Express (servidor)").arquivo("server.js", "import express from \"express\";\n\nconst app = express();\n\napp.get(\"/\", (req, res) => res.send(\"<h1>Olá do Express!</h1>\"));\n\napp.listen(3000, () => console.log(\"Rodando em http://localhost:3000\"));\n")
            .arquivo("package.json", pkg).abrir("server.js").instalar("npm install express").rodar("node server.js").url("http://localhost:3000").precisa(PRECISA_NODE);
        m(NODE, "Fastify (servidor rápido)").arquivo("server.js", "import Fastify from \"fastify\";\n\nconst app = Fastify();\n\napp.get(\"/\", async () => ({ mensagem: \"Olá do Fastify!\" }));\n\nawait app.listen({ port: 3000 });\nconsole.log(\"Rodando em http://localhost:3000\");\n")
            .arquivo("package.json", pkg).abrir("server.js").instalar("npm install fastify").rodar("node server.js").url("http://localhost:3000").precisa(PRECISA_NODE);
        m(NODE, "Koa (servidor)").arquivo("server.js", "import Koa from \"koa\";\n\nconst app = new Koa();\n\napp.use(ctx => { ctx.body = \"<h1>Olá do Koa!</h1>\"; });\n\napp.listen(3000, () => console.log(\"Rodando em http://localhost:3000\"));\n")
            .arquivo("package.json", pkg).abrir("server.js").instalar("npm install koa").rodar("node server.js").url("http://localhost:3000").precisa(PRECISA_NODE);
        m(NODE, "Hono (servidor moderno)").arquivo("server.js", "import { Hono } from \"hono\";\nimport { serve } from \"@hono/node-server\";\n\nconst app = new Hono();\n\napp.get(\"/\", c => c.html(\"<h1>Olá do Hono!</h1>\"));\n\nserve({ fetch: app.fetch, port: 3000 });\nconsole.log(\"Rodando em http://localhost:3000\");\n")
            .arquivo("package.json", pkg).abrir("server.js").instalar("npm install hono @hono/node-server").rodar("node server.js").url("http://localhost:3000").precisa(PRECISA_NODE);
        m(NODE, "EJS (páginas com Express)").arquivo("server.js", "import express from \"express\";\n\nconst app = express();\napp.set(\"view engine\", \"ejs\");\n\napp.get(\"/\", (req, res) => res.render(\"inicio\", { nome: \"João\", jogos: [\"Minecraft\", \"Roblox\"] }));\n\napp.listen(3000, () => console.log(\"Rodando em http://localhost:3000\"));\n")
            .arquivo("views/inicio.ejs", "<h1>Olá, <%= nome %>!</h1>\n<ul>\n<% jogos.forEach(j => { %>\n  <li><%= j %></li>\n<% }) %>\n</ul>\n")
            .arquivo("package.json", pkg).abrir("server.js").instalar("npm install express ejs").rodar("node server.js").url("http://localhost:3000").precisa(PRECISA_NODE);
        m(NODE, "NestJS (servidor grande)").arquivo("LEIA-ME.txt", "Projeto NestJS: o TazzCode cria a pasta \"app\" com o comando oficial na primeira vez.\nDepois aperte F5 e abra http://localhost:3000\n")
            .instalar("npx -y @nestjs/cli new app --package-manager npm --skip-git").rodar("npm --prefix app run start").url("http://localhost:3000").precisa(PRECISA_NODE);

        // ===================== JAVA =====================
        m(JAVA, "Java Swing (janela)").arquivo("Janela.java", "import javax.swing.*;\n\npublic class Janela {\n    public static void main(String[] args) {\n        JFrame janela = new JFrame(\"Minha janela\");\n        JButton botao = new JButton(\"Clique aqui\");\n        botao.addActionListener(e -> JOptionPane.showMessageDialog(janela, \"Olá do Swing!\"));\n        janela.add(botao);\n        janela.setSize(300, 150);\n        janela.setLocationRelativeTo(null);\n        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n        janela.setVisible(true);\n    }\n}\n")
            .rodar("java Janela.java").precisa("JDK: https://adoptium.net");
        m(JAVA, "JavaFX (janela moderna)").arquivo("pom.xml", pom("javafx", "<dependency><groupId>org.openjfx</groupId><artifactId>javafx-controls</artifactId><version>21.0.5</version></dependency>",
                "<plugin><groupId>org.openjfx</groupId><artifactId>javafx-maven-plugin</artifactId><version>0.0.8</version><configuration><mainClass>App</mainClass></configuration></plugin>"))
            .arquivo("src/main/java/App.java", "import javafx.application.Application;\nimport javafx.scene.Scene;\nimport javafx.scene.control.Button;\nimport javafx.stage.Stage;\n\npublic class App extends Application {\n    @Override\n    public void start(Stage palco) {\n        Button botao = new Button(\"Olá do JavaFX!\");\n        botao.setOnAction(e -> botao.setText(\"Você clicou!\"));\n        palco.setScene(new Scene(botao, 300, 150));\n        palco.setTitle(\"Minha janela\");\n        palco.show();\n    }\n\n    public static void main(String[] args) {\n        launch(args);\n    }\n}\n")
            .abrir("src/main/java/App.java").rodar("mvn -q javafx:run").precisa(PRECISA_MVN);
        m(JAVA, "Spring Boot (servidor)").arquivo("pom.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n  <modelVersion>4.0.0</modelVersion>\n  <parent>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-parent</artifactId>\n    <version>3.3.5</version>\n  </parent>\n  <groupId>tazzcode</groupId>\n  <artifactId>app</artifactId>\n  <version>1.0</version>\n  <properties><java.version>21</java.version></properties>\n  <dependencies>\n    <dependency>\n      <groupId>org.springframework.boot</groupId>\n      <artifactId>spring-boot-starter-web</artifactId>\n    </dependency>\n  </dependencies>\n  <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>\n</project>\n")
            .arquivo("src/main/java/app/App.java", "package app;\n\nimport org.springframework.boot.SpringApplication;\nimport org.springframework.boot.autoconfigure.SpringBootApplication;\nimport org.springframework.web.bind.annotation.GetMapping;\nimport org.springframework.web.bind.annotation.RestController;\n\n@SpringBootApplication\n@RestController\npublic class App {\n    public static void main(String[] args) {\n        SpringApplication.run(App.class, args);\n    }\n\n    @GetMapping(\"/\")\n    public String inicio() {\n        return \"Olá do Spring Boot!\";\n    }\n}\n")
            .abrir("src/main/java/app/App.java").rodar("mvn -q spring-boot:run").url("http://localhost:8080").precisa(PRECISA_MVN);
        m(JAVA, "Javalin (servidor leve)").arquivo("pom.xml", pom("javalin", "<dependency><groupId>io.javalin</groupId><artifactId>javalin</artifactId><version>6.3.0</version></dependency><dependency><groupId>org.slf4j</groupId><artifactId>slf4j-simple</artifactId><version>2.0.16</version></dependency>",
                "<plugin><groupId>org.codehaus.mojo</groupId><artifactId>exec-maven-plugin</artifactId><version>3.5.0</version><configuration><mainClass>App</mainClass></configuration></plugin>"))
            .arquivo("src/main/java/App.java", "import io.javalin.Javalin;\n\npublic class App {\n    public static void main(String[] args) {\n        Javalin.create().get(\"/\", ctx -> ctx.result(\"Olá do Javalin!\")).start(7070);\n    }\n}\n")
            .abrir("src/main/java/App.java").rodar("mvn -q compile exec:java").url("http://localhost:7070").precisa(PRECISA_MVN);

        // ===================== GO =====================
        goWeb("Gin", "github.com/gin-gonic/gin", "package main\n\nimport \"github.com/gin-gonic/gin\"\n\nfunc main() {\n    r := gin.Default()\n    r.GET(\"/\", func(c *gin.Context) {\n        c.String(200, \"Olá do Gin!\")\n    })\n    r.Run(\":8080\")\n}\n", "http://localhost:8080");
        goWeb("Echo", "github.com/labstack/echo/v4", "package main\n\nimport (\n    \"net/http\"\n\n    \"github.com/labstack/echo/v4\"\n)\n\nfunc main() {\n    e := echo.New()\n    e.GET(\"/\", func(c echo.Context) error {\n        return c.String(http.StatusOK, \"Olá do Echo!\")\n    })\n    e.Logger.Fatal(e.Start(\":1323\"))\n}\n", "http://localhost:1323");
        goWeb("Fiber", "github.com/gofiber/fiber/v2", "package main\n\nimport \"github.com/gofiber/fiber/v2\"\n\nfunc main() {\n    app := fiber.New()\n    app.Get(\"/\", func(c *fiber.Ctx) error {\n        return c.SendString(\"Olá do Fiber!\")\n    })\n    app.Listen(\":3000\")\n}\n", "http://localhost:3000");

        // ===================== OUTRAS =====================
        m(OUTROS, "Rust: Axum (servidor)").arquivo("Cargo.toml", "[package]\nname = \"app\"\nversion = \"0.1.0\"\nedition = \"2021\"\n\n[dependencies]\naxum = \"0.7\"\ntokio = { version = \"1\", features = [\"full\"] }\n")
            .arquivo("src/main.rs", "use axum::{routing::get, Router};\n\n#[tokio::main]\nasync fn main() {\n    let app = Router::new().route(\"/\", get(|| async { \"Olá do Axum!\" }));\n    let ouvinte = tokio::net::TcpListener::bind(\"127.0.0.1:3000\").await.unwrap();\n    println!(\"Rodando em http://localhost:3000\");\n    axum::serve(ouvinte, app).await.unwrap();\n}\n")
            .abrir("src/main.rs").rodar("cargo run").url("http://localhost:3000").precisa("Rust: https://rustup.rs");
        m(OUTROS, "Ruby on Rails (site grande)").arquivo("LEIA-ME.txt", "Projeto Rails: o TazzCode instala o Rails e cria o projeto na primeira vez (demora!).\n")
            .instalar("gem install rails && rails new . --skip-git --force").rodar("ruby bin/rails server").url("http://localhost:3000").precisa("Ruby: https://rubyinstaller.org");
        m(OUTROS, "PHP Web (site em PHP)").arquivo("index.php", "<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head><meta charset=\"UTF-8\"><title>PHP</title></head>\n<body>\n    <h1>Olá do PHP!</h1>\n    <p>Agora são <?= date('H:i:s') ?></p>\n</body>\n</html>\n")
            .rodar("php -S localhost:8000").url("http://localhost:8000").precisa("PHP: https://windows.php.net/download");
        m(OUTROS, "Laravel (PHP grande)").arquivo("LEIA-ME.txt", "Projeto Laravel: o TazzCode cria com o Composer na primeira vez.\n")
            .instalar("composer create-project laravel/laravel app").rodar("php app/artisan serve").url("http://127.0.0.1:8000").precisa("PHP + Composer: https://getcomposer.org");
        m(OUTROS, "Symfony (PHP grande)").arquivo("LEIA-ME.txt", "Projeto Symfony: o TazzCode cria com o Composer na primeira vez.\n")
            .instalar("composer create-project symfony/skeleton app").rodar("php -S localhost:8000 -t app/public").url("http://localhost:8000").precisa("PHP + Composer: https://getcomposer.org");
        m(OUTROS, "C: raylib (jogo)").arquivo("main.c", "#include \"raylib.h\"\n\nint main(void) {\n    InitWindow(640, 480, \"Meu jogo com raylib\");\n    SetTargetFPS(60);\n    Vector2 pos = { 320, 240 };\n    while (!WindowShouldClose()) {\n        if (IsKeyDown(KEY_RIGHT)) pos.x += 4;\n        if (IsKeyDown(KEY_LEFT)) pos.x -= 4;\n        if (IsKeyDown(KEY_UP)) pos.y -= 4;\n        if (IsKeyDown(KEY_DOWN)) pos.y += 4;\n        BeginDrawing();\n        ClearBackground((Color){ 27, 34, 51, 255 });\n        DrawCircleV(pos, 25, SKYBLUE);\n        DrawText(\"Use as setas!\", 10, 10, 20, RAYWHITE);\n        EndDrawing();\n    }\n    CloseWindow();\n    return 0;\n}\n")
            .rodar("gcc main.c -o {saida} -lraylib -lopengl32 -lgdi32 -lwinmm && {saida}").precisa("gcc + raylib (no MSYS2: pacman -S mingw-w64-ucrt-x86_64-raylib)");
        m(OUTROS, "C#: Avalonia (janela)").arquivo("LEIA-ME.txt", "Projeto Avalonia: o TazzCode cria com o dotnet na primeira vez.\n")
            .instalar("dotnet new install Avalonia.Templates && dotnet new avalonia.app -o app").rodar("dotnet run --project app").precisa(".NET SDK: https://dotnet.microsoft.com/download");

        // ===================== WEB (CDN) =====================
        web("HTML puro (HTML + CSS + JS)", "", "<h1>Olá, mundo!</h1>\n    <button id=\"botao\">Clique</button>",
                "document.getElementById(\"botao\").onclick = () => alert(\"Olá!\");", false);
        web("Bootstrap", css("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css") + js("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"),
                "<div class=\"container py-5\">\n        <h1 class=\"text-primary\">Olá do Bootstrap!</h1>\n        <button class=\"btn btn-primary\">Botão bonito</button>\n    </div>", "", false);
        web("Tailwind CSS", js("https://cdn.tailwindcss.com"),
                "<div class=\"min-h-screen bg-slate-900 flex items-center justify-center\">\n        <h1 class=\"text-4xl font-bold text-sky-400\">Olá do Tailwind!</h1>\n    </div>", "", false);
        web("daisyUI", css("https://cdn.jsdelivr.net/npm/daisyui@4.12.14/dist/full.min.css") + js("https://cdn.tailwindcss.com"),
                "<div class=\"p-10\">\n        <h1 class=\"text-3xl mb-4\">Olá do daisyUI!</h1>\n        <button class=\"btn btn-primary\">Botão</button>\n    </div>", "", false);
        web("Bulma", css("https://cdn.jsdelivr.net/npm/bulma@1.0.2/css/bulma.min.css"),
                "<section class=\"section\">\n        <h1 class=\"title\">Olá do Bulma!</h1>\n        <button class=\"button is-link\">Botão</button>\n    </section>", "", false);
        web("Foundation", css("https://cdn.jsdelivr.net/npm/foundation-sites@6.8.1/dist/css/foundation.min.css"),
                "<div class=\"grid-container\">\n        <h1>Olá do Foundation!</h1>\n        <a class=\"button\">Botão</a>\n    </div>", "", false);
        web("Materialize", css("https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.1.1/dist/css/materialize.min.css"),
                "<div class=\"container\">\n        <h1>Olá do Materialize!</h1>\n        <a class=\"btn waves-effect\">Botão</a>\n    </div>", "", false);
        web("UIkit", css("https://cdn.jsdelivr.net/npm/uikit@3.21.13/dist/css/uikit.min.css") + js("https://cdn.jsdelivr.net/npm/uikit@3.21.13/dist/js/uikit.min.js"),
                "<div class=\"uk-container uk-margin-top\">\n        <h1>Olá do UIkit!</h1>\n        <button class=\"uk-button uk-button-primary\">Botão</button>\n    </div>", "", false);
        web("Semantic UI (Fomantic)", css("https://cdn.jsdelivr.net/npm/fomantic-ui@2.9.3/dist/semantic.min.css"),
                "<div class=\"ui container\" style=\"margin-top:2em\">\n        <h1 class=\"ui header\">Olá do Semantic UI!</h1>\n        <button class=\"ui primary button\">Botão</button>\n    </div>", "", false);
        web("Skeleton", css("https://cdnjs.cloudflare.com/ajax/libs/skeleton/2.0.4/skeleton.min.css"),
                "<div class=\"container\">\n        <h1>Olá do Skeleton!</h1>\n        <button class=\"button-primary\">Botão</button>\n    </div>", "", false);
        web("Milligram", css("https://cdnjs.cloudflare.com/ajax/libs/milligram/1.4.1/milligram.min.css"),
                "<div class=\"container\">\n        <h1>Olá do Milligram!</h1>\n        <button>Botão</button>\n    </div>", "", false);
        web("PaperCSS", css("https://unpkg.com/papercss@1.9.2/dist/paper.min.css"),
                "<div class=\"paper container\">\n        <h1>Olá do PaperCSS!</h1>\n        <button>Botão desenhado à mão</button>\n    </div>", "", false);
        web("Sass / SCSS", "", "<h1 class=\"titulo\">Olá do Sass!</h1>", "", false)
            .arquivo("style.scss", "$azul: #2F7DF6;\n\nbody {\n    background: #1B2233;\n    font-family: sans-serif;\n\n    .titulo {\n        color: $azul;\n        &:hover { color: lighten($azul, 20%); }\n    }\n}\n")
            .abrir("style.scss").rodar("npx -y sass style.scss style.css && servidor:index.html").precisa(PRECISA_NODE + " (o Sass vira CSS antes de abrir)");
        web("jQuery", js("https://code.jquery.com/jquery-3.7.1.min.js"), "<h1>Olá do jQuery!</h1>\n    <button>Esconder o título</button>",
                "$(\"button\").on(\"click\", () => $(\"h1\").toggle());", false);
        web("Alpine.js", "<script defer src=\"https://cdn.jsdelivr.net/npm/alpinejs@3.14.3/dist/cdn.min.js\"></script>\n",
                "<div x-data=\"{ cliques: 0 }\">\n        <h1>Olá do Alpine!</h1>\n        <button @click=\"cliques++\">Cliques: <span x-text=\"cliques\"></span></button>\n    </div>", "", false);
        web("HTMX", js("https://unpkg.com/htmx.org@2.0.3"),
                "<h1>Olá do HTMX!</h1>\n    <button hx-get=\"https://httpbin.org/uuid\" hx-target=\"#resultado\">Buscar um código</button>\n    <pre id=\"resultado\"></pre>", "", false);
        web("BackboneJS", js("https://code.jquery.com/jquery-3.7.1.min.js") + js("https://cdn.jsdelivr.net/npm/underscore@1.13.7/underscore-umd-min.js") + js("https://cdn.jsdelivr.net/npm/backbone@1.6.0/backbone-min.js"),
                "<div id=\"app\"></div>", "const Ola = Backbone.View.extend({\n    el: \"#app\",\n    render() {\n        this.$el.html(\"<h1>Olá do Backbone!</h1>\");\n        return this;\n    }\n});\nnew Ola().render();", false);
        web("Vue", js("https://unpkg.com/vue@3/dist/vue.global.prod.js"),
                "<div id=\"app\">\n        <h1>{{ mensagem }}</h1>\n        <button @click=\"cliques++\">Cliques: {{ cliques }}</button>\n    </div>",
                "Vue.createApp({\n    data() { return { mensagem: \"Olá do Vue!\", cliques: 0 }; }\n}).mount(\"#app\");", false);
        web("Vuetify (Vue + Material)", css("https://cdn.jsdelivr.net/npm/vuetify@3.7.4/dist/vuetify.min.css") + js("https://unpkg.com/vue@3/dist/vue.global.prod.js") + js("https://cdn.jsdelivr.net/npm/vuetify@3.7.4/dist/vuetify.min.js"),
                "<div id=\"app\">\n        <v-app><v-container>\n            <h1>Olá do Vuetify!</h1>\n            <v-btn color=\"primary\" @click=\"cliques++\">Cliques: {{ cliques }}</v-btn>\n        </v-container></v-app>\n    </div>",
                "Vue.createApp({ data: () => ({ cliques: 0 }) }).use(Vuetify.createVuetify()).mount(\"#app\");", false);
        web("Preact", "", "<div id=\"app\"></div>",
                "import { h, render } from \"https://esm.sh/preact@10\";\nimport { useState } from \"https://esm.sh/preact@10/hooks\";\n\nfunction App() {\n    const [n, setN] = useState(0);\n    return h(\"div\", null, h(\"h1\", null, \"Olá do Preact!\"), h(\"button\", { onClick: () => setN(n + 1) }, \"Cliques: \" + n));\n}\nrender(h(App), document.getElementById(\"app\"));", true);
        web("Lit (Web Components)", "", "<meu-ola></meu-ola>",
                "import { LitElement, html } from \"https://esm.sh/lit@3\";\n\nclass MeuOla extends LitElement {\n    static properties = { cliques: { type: Number } };\n    constructor() { super(); this.cliques = 0; }\n    render() {\n        return html`<h1>Olá do Lit!</h1><button @click=${() => this.cliques++}>Cliques: ${this.cliques}</button>`;\n    }\n}\ncustomElements.define(\"meu-ola\", MeuOla);", true);
        web("Chart.js (gráficos)", js("https://cdn.jsdelivr.net/npm/chart.js@4.4.6"), "<canvas id=\"grafico\" width=\"600\" height=\"300\"></canvas>",
                "new Chart(document.getElementById(\"grafico\"), {\n    type: \"bar\",\n    data: { labels: [\"Seg\", \"Ter\", \"Qua\"], datasets: [{ label: \"Moedas\", data: [12, 19, 7] }] }\n});", false);
        web("D3.js (gráficos)", js("https://cdn.jsdelivr.net/npm/d3@7"), "<svg width=\"400\" height=\"200\"></svg>",
                "const dados = [30, 80, 45, 60];\nd3.select(\"svg\").selectAll(\"rect\").data(dados).join(\"rect\")\n    .attr(\"x\", (d, i) => i * 90).attr(\"y\", d => 200 - d * 2)\n    .attr(\"width\", 70).attr(\"height\", d => d * 2).attr(\"fill\", \"#2F7DF6\");", false);
        web("ECharts (gráficos)", js("https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js"), "<div id=\"grafico\" style=\"width:600px;height:300px\"></div>",
                "echarts.init(document.getElementById(\"grafico\")).setOption({\n    xAxis: { data: [\"Seg\", \"Ter\", \"Qua\"] }, yAxis: {},\n    series: [{ type: \"line\", data: [5, 20, 12] }]\n});", false);
        web("Three.js (3D)", "", "",
                "import * as THREE from \"https://esm.sh/three@0.170.0\";\n\nconst cena = new THREE.Scene();\nconst camera = new THREE.PerspectiveCamera(70, innerWidth / innerHeight, 0.1, 100);\ncamera.position.z = 3;\nconst render = new THREE.WebGLRenderer();\nrender.setSize(innerWidth, innerHeight);\ndocument.body.appendChild(render.domElement);\nconst cubo = new THREE.Mesh(new THREE.BoxGeometry(), new THREE.MeshNormalMaterial());\ncena.add(cubo);\nrender.setAnimationLoop(() => {\n    cubo.rotation.x += 0.01;\n    cubo.rotation.y += 0.02;\n    render.render(cena, camera);\n});", true);
        web("PixiJS (2D rápido)", "", "",
                "import { Application, Graphics } from \"https://esm.sh/pixi.js@8\";\n\nconst app = new Application();\nawait app.init({ width: 640, height: 400, background: \"#1B2233\" });\ndocument.body.appendChild(app.canvas);\nconst bola = new Graphics().circle(0, 0, 30).fill(\"#2F7DF6\");\nbola.position.set(320, 200);\napp.stage.addChild(bola);\napp.ticker.add(t => { bola.x = 320 + Math.sin(performance.now() / 500) * 200; });", true);
        web("Phaser (jogos)", js("https://cdn.jsdelivr.net/npm/phaser@3.86.0/dist/phaser.min.js"), "",
                "new Phaser.Game({\n    width: 640, height: 400, backgroundColor: \"#1B2233\",\n    scene: {\n        create() {\n            this.add.text(220, 180, \"Olá do Phaser!\", { fontSize: \"28px\", color: \"#7CC4FF\" });\n        }\n    }\n});", false);
        web("p5.js (desenho criativo)", js("https://cdn.jsdelivr.net/npm/p5@1.11.1/lib/p5.min.js"), "",
                "function setup() { createCanvas(600, 400); }\n\nfunction draw() {\n    background(27, 34, 51);\n    fill(47, 125, 246);\n    circle(mouseX, mouseY, 50);\n}", false);
        web("Anime.js (animações)", js("https://cdn.jsdelivr.net/npm/animejs@3.2.2/lib/anime.min.js"), "<div class=\"caixa\" style=\"width:60px;height:60px;background:#2F7DF6\"></div>",
                "anime({ targets: \".caixa\", translateX: 300, rotate: 360, loop: true, direction: \"alternate\", duration: 1500 });", false);
        web("GSAP (animações)", js("https://cdn.jsdelivr.net/npm/gsap@3.12.5/dist/gsap.min.js"), "<div class=\"caixa\" style=\"width:60px;height:60px;background:#2F7DF6\"></div>",
                "gsap.to(\".caixa\", { x: 300, rotation: 360, duration: 2, repeat: -1, yoyo: true });", false);
        web("Motion (animações)", "", "<div class=\"caixa\" style=\"width:60px;height:60px;background:#2F7DF6\"></div>",
                "import { animate } from \"https://esm.sh/motion@11\";\n\nanimate(\".caixa\", { x: 300, rotate: 360 }, { duration: 2, repeat: Infinity, repeatType: \"reverse\" });", true);
        web("Leaflet (mapas)", css("https://unpkg.com/leaflet@1.9.4/dist/leaflet.css") + js("https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"), "<div id=\"mapa\" style=\"height:450px\"></div>",
                "const mapa = L.map(\"mapa\").setView([-10.184, -48.333], 13);   // Palmas - TO\nL.tileLayer(\"https://tile.openstreetmap.org/{z}/{x}/{y}.png\", { attribution: \"© OpenStreetMap\" }).addTo(mapa);\nL.marker([-10.184, -48.333]).addTo(mapa).bindPopup(\"Palmas!\").openPopup();", false);
        web("Mermaid (diagramas)", "", "<pre class=\"mermaid\">\nflowchart LR\n    A[Ideia] --> B[Código]\n    B --> C{Funcionou?}\n    C -- Sim --> D[Festa!]\n    C -- Não --> B\n    </pre>",
                "import mermaid from \"https://esm.sh/mermaid@11\";\nmermaid.initialize({ startOnLoad: true, theme: \"dark\" });", true);
        web("Reveal.js (apresentação)", css("https://cdn.jsdelivr.net/npm/reveal.js@5.1.0/dist/reveal.css") + css("https://cdn.jsdelivr.net/npm/reveal.js@5.1.0/dist/theme/black.css") + js("https://cdn.jsdelivr.net/npm/reveal.js@5.1.0/dist/reveal.js"),
                "<div class=\"reveal\"><div class=\"slides\">\n        <section><h1>Minha apresentação</h1></section>\n        <section><h2>Slide 2</h2><p>Use as setas!</p></section>\n    </div></div>", "Reveal.initialize();", false);
        web("Marked (Markdown)", js("https://cdn.jsdelivr.net/npm/marked/marked.min.js"), "<div id=\"saida\"></div>",
                "document.getElementById(\"saida\").innerHTML = marked.parse(\"# Olá do Marked!\\n\\nIsso é **Markdown** virando HTML.\\n\\n- item 1\\n- item 2\");", false);
        web("KaTeX (fórmulas)", css("https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.css") + js("https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.js"), "<div id=\"formula\" style=\"font-size:2em\"></div>",
                "katex.render(\"x = \\\\frac{-b \\\\pm \\\\sqrt{b^2 - 4ac}}{2a}\", document.getElementById(\"formula\"));", false);
        web("Ionic (app de celular)", "<script type=\"module\" src=\"https://cdn.jsdelivr.net/npm/@ionic/core@8/dist/ionic/ionic.esm.js\"></script>\n" + css("https://cdn.jsdelivr.net/npm/@ionic/core@8/css/ionic.bundle.css"),
                "<ion-app>\n        <ion-header><ion-toolbar><ion-title>Meu app</ion-title></ion-toolbar></ion-header>\n        <ion-content class=\"ion-padding\">\n            <h1>Olá do Ionic!</h1>\n            <ion-button>Botão</ion-button>\n        </ion-content>\n    </ion-app>", "", false);

        // ===================== WEB COM REACT (sem instalar nada) =====================
        react("React", "", "function App() {\n    const [n, setN] = useState(0);\n    return (\n        <div>\n            <h1>Olá do React!</h1>\n            <button onClick={() => setN(n + 1)}>Cliques: {n}</button>\n        </div>\n    );\n}");
        react("MUI (Material UI)", "import { Button, Typography, Container } from \"@mui/material\";\n",
                "function App() {\n    return (\n        <Container sx={{ mt: 4 }}>\n            <Typography variant=\"h3\">Olá do MUI!</Typography>\n            <Button variant=\"contained\">Botão</Button>\n        </Container>\n    );\n}", "@mui/material", "https://esm.sh/@mui/material@6?deps=react@18,react-dom@18");
        react("Chakra UI", "import { ChakraProvider, defaultSystem, Button, Heading } from \"@chakra-ui/react\";\n",
                "function App() {\n    return (\n        <ChakraProvider value={defaultSystem}>\n            <Heading>Olá do Chakra!</Heading>\n            <Button colorPalette=\"blue\">Botão</Button>\n        </ChakraProvider>\n    );\n}", "@chakra-ui/react", "https://esm.sh/@chakra-ui/react@3?deps=react@18,react-dom@18");
        react("Mantine", "import { MantineProvider, Button, Title } from \"@mantine/core\";\n",
                "function App() {\n    return (\n        <MantineProvider defaultColorScheme=\"dark\">\n            <Title>Olá do Mantine!</Title>\n            <Button>Botão</Button>\n        </MantineProvider>\n    );\n}", "@mantine/core", "https://esm.sh/@mantine/core@7?deps=react@18,react-dom@18")
            .arquivos.computeIfPresent("index.html", (k, v) -> v.replace("<title>", "<link rel=\"stylesheet\" href=\"https://esm.sh/@mantine/core@7/styles.css\">\n    <title>"));
        react("React Bootstrap", "import { Button, Container } from \"react-bootstrap\";\n",
                "function App() {\n    return (\n        <Container className=\"py-4\">\n            <h1>Olá do React Bootstrap!</h1>\n            <Button>Botão</Button>\n        </Container>\n    );\n}", "react-bootstrap", "https://esm.sh/react-bootstrap@2?deps=react@18,react-dom@18")
            .arquivos.computeIfPresent("index.html", (k, v) -> v.replace("<title>", css("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css") + "    <title>"));
        react("Radix UI", "import * as Switch from \"@radix-ui/react-switch\";\n",
                "function App() {\n    const [ligado, setLigado] = useState(false);\n    return (\n        <div>\n            <h1>Olá do Radix!</h1>\n            <Switch.Root checked={ligado} onCheckedChange={setLigado} style={{ width: 42, height: 24, background: ligado ? \"#2F7DF6\" : \"#555\", borderRadius: 12 }}>\n                <Switch.Thumb style={{ display: \"block\", width: 20, height: 20, background: \"white\", borderRadius: 10, transform: ligado ? \"translateX(20px)\" : \"none\" }} />\n            </Switch.Root>\n            <p>{ligado ? \"Ligado\" : \"Desligado\"}</p>\n        </div>\n    );\n}", "@radix-ui/react-switch", "https://esm.sh/@radix-ui/react-switch@1?deps=react@18,react-dom@18");
        react("Headless UI", "import { Switch } from \"@headlessui/react\";\n",
                "function App() {\n    const [ligado, setLigado] = useState(false);\n    return (\n        <div>\n            <h1>Olá do Headless UI!</h1>\n            <Switch checked={ligado} onChange={setLigado} style={{ width: 42, height: 24, background: ligado ? \"#2F7DF6\" : \"#555\", borderRadius: 12, border: 0 }} />\n            <p>{ligado ? \"Ligado\" : \"Desligado\"}</p>\n        </div>\n    );\n}", "@headlessui/react", "https://esm.sh/@headlessui/react@2?deps=react@18,react-dom@18");
        react("PrimeReact", "import { Button } from \"primereact/button\";\n",
                "function App() {\n    return (\n        <div>\n            <h1>Olá do PrimeReact!</h1>\n            <Button label=\"Botão\" />\n        </div>\n    );\n}", "primereact/button", "https://esm.sh/primereact@10/button?deps=react@18,react-dom@18")
            .arquivos.computeIfPresent("index.html", (k, v) -> v.replace("<title>", css("https://unpkg.com/primereact@10/resources/themes/lara-dark-blue/theme.css") + "    <title>"));
        react("Recharts (gráficos React)", "import { BarChart, Bar, XAxis, YAxis } from \"recharts\";\n",
                "const dados = [{ dia: \"Seg\", moedas: 12 }, { dia: \"Ter\", moedas: 19 }, { dia: \"Qua\", moedas: 7 }];\n\nfunction App() {\n    return (\n        <div>\n            <h1>Olá do Recharts!</h1>\n            <BarChart width={500} height={250} data={dados}>\n                <XAxis dataKey=\"dia\" /><YAxis />\n                <Bar dataKey=\"moedas\" fill=\"#2F7DF6\" />\n            </BarChart>\n        </div>\n    );\n}", "recharts", "https://esm.sh/recharts@2?deps=react@18,react-dom@18");

        // ===================== WEB GRANDE (cria com npm) =====================
        grande("Next.js", "npx -y create-next-app@latest app --yes", "npm --prefix app run dev", "http://localhost:3000");
        grande("Angular", "npx -y @angular/cli@latest new app --defaults --skip-git", "npm --prefix app start", "http://localhost:4200");
        grande("Svelte (com Vite)", "npm create -y vite@latest app -- --template svelte && npm --prefix app install", "npm --prefix app run dev", "http://localhost:5173");
        grande("React (com Vite)", "npm create -y vite@latest app -- --template react && npm --prefix app install", "npm --prefix app run dev", "http://localhost:5173");
        grande("shadcn/ui (React + Tailwind)", "npm create -y vite@latest app -- --template react-ts && npm --prefix app install", "npm --prefix app run dev", "http://localhost:5173")
            .arquivos.put("LEIA-ME.txt", "shadcn/ui: depois de criar, siga https://ui.shadcn.com/docs/installation/vite\n(no terminal, dentro da pasta app: npx shadcn@latest init)\n");
    }

    // =====================================================================
    //  AJUDANTES
    // =====================================================================

    private static String css(String url) { return "<link rel=\"stylesheet\" href=\"" + url + "\">\n"; }
    private static String js(String url) { return "<script src=\"" + url + "\"></script>\n"; }

    /** Uma página web com bibliotecas pela internet (CDN). */
    private static Modelo web(String nome, String cabeca, String corpo, String script, boolean modulo) {
        String html = "<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head>\n    <meta charset=\"UTF-8\">\n"
                + (cabeca.isEmpty() ? "" : "    " + cabeca.trim().replace("\n", "\n    ") + "\n")
                + "    <title>" + nome + "</title>\n    <link rel=\"stylesheet\" href=\"style.css\">\n</head>\n<body>\n"
                + (corpo.isEmpty() ? "" : "    " + corpo + "\n")
                + (script.isEmpty() ? "" : "    <script" + (modulo ? " type=\"module\"" : "") + " src=\"script.js\"></script>\n")
                + "</body>\n</html>\n";
        Modelo x = m(WEB, nome).arquivo("index.html", html)
                .arquivo("style.css", "body {\n    font-family: sans-serif;\n    margin: 0;\n    padding: 20px;\n}\n")
                .rodar("servidor:index.html").precisa("Só um navegador (e internet, pra baixar a biblioteca). O TazzCode liga um servidorzinho sozinho.");
        if (!script.isEmpty()) x.arquivo("script.js", script + "\n");
        x.abrir(script.isEmpty() ? "index.html" : "script.js");
        return x;
    }

    /** Uma página com React (JSX funciona direto no navegador, sem instalar nada). */
    private static Modelo react(String nome, String imports, String app, String... pacote) {
        String mapa = "{\n            \"imports\": {\n                \"react\": \"https://esm.sh/react@18\",\n                \"react-dom/client\": \"https://esm.sh/react-dom@18/client\""
                + (pacote.length == 2 ? ",\n                \"" + pacote[0] + "\": \"" + pacote[1] + "\"" : "") + "\n            }\n        }";
        String html = "<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>" + nome + "</title>\n"
                + "    <script type=\"importmap\">\n        " + mapa + "\n    </script>\n"
                + "    <script src=\"https://unpkg.com/@babel/standalone@7/babel.min.js\"></script>\n</head>\n<body>\n    <div id=\"root\"></div>\n"
                + "    <script type=\"text/babel\" data-type=\"module\" src=\"app.jsx\"></script>\n</body>\n</html>\n";
        String jsx = "import React, { useState } from \"react\";\nimport { createRoot } from \"react-dom/client\";\n" + imports + "\n" + app
                + "\n\ncreateRoot(document.getElementById(\"root\")).render(<App />);\n";
        return m(REACT, nome).arquivo("index.html", html).arquivo("app.jsx", jsx).abrir("app.jsx")
                .rodar("servidor:index.html").precisa("Só um navegador e internet. O TazzCode liga um servidorzinho sozinho (o React precisa).");
    }

    private static Modelo grande(String nome, String criar, String rodar, String url) {
        return m(GRANDES, nome).arquivo("LEIA-ME.txt", nome + ": o TazzCode cria o projeto (pasta \"app\") com o comando oficial na primeira vez.\n"
                        + "Isso baixa bastante coisa da internet, então pode demorar uns minutos.\nDepois aperte F5 e abra " + url + "\n")
                .instalar(criar).rodar(rodar).url(url).precisa(PRECISA_NODE);
    }

    private static void goWeb(String nome, String modulo, String codigo, String url) {
        m(GO, nome + " (servidor)").arquivo("main.go", codigo).arquivo("go.mod", "module app\n\ngo 1.22\n")
                .abrir("main.go").instalar("go get " + modulo + " && go mod tidy").rodar("go run .").url(url).precisa("Go: https://go.dev/dl");
    }

    private static String pom(String nome, String dependencias, String plugin) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n  <modelVersion>4.0.0</modelVersion>\n"
                + "  <groupId>tazzcode</groupId>\n  <artifactId>" + nome + "</artifactId>\n  <version>1.0</version>\n"
                + "  <properties>\n    <maven.compiler.release>21</maven.compiler.release>\n    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n  </properties>\n"
                + "  <dependencies>\n    " + dependencias + "\n  </dependencies>\n  <build><plugins>\n    " + plugin + "\n  </plugins></build>\n</project>\n";
    }
}
