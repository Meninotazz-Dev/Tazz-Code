// Site do TazzCode - scripts
{
const linguagens=["Java","Python","C","C++","C#","JavaScript","TypeScript","HTML","CSS","Go","Rust","Kotlin","PHP","Ruby","Lua","SQL","Dart","Swift","Haskell","Pascal","Assembly","Brainfuck","+ 92"];
const chips=document.getElementById('chips');
if(chips)chips.innerHTML=linguagens.map(l=>'<span>'+l+'</span>').join('');
}

// Botões "Copiar" da página de downloads
{
for(const b of document.querySelectorAll('.cmd button')){
  b.addEventListener('click',async()=>{
    const texto=b.parentElement.querySelector('code').textContent;
    try{await navigator.clipboard.writeText(texto);b.textContent='Copiado!';}
    catch(e){b.textContent='Selecione e copie';}
    setTimeout(()=>{b.textContent='Copiar';},1800);
  });
}
}
