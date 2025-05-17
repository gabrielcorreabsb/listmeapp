// Variáveis globais
let currentSection = 0;
const sections = document.querySelectorAll('.section');
const dots = document.querySelectorAll('.nav-dot');
let isScrolling = false;
let touchStartY = 0;
let touchEndY = 0;
let isMobile = window.innerWidth <= 992;

// Função para inicialização
document.addEventListener('DOMContentLoaded', function() {
  // Verifica se é dispositivo móvel
  checkMobile();

  // Inicializa o slideshow
  initSlideshow();

  // Inicializa os eventos
  initEvents();

  // Atualiza estado inicial
  updateState();
});

// Função para verificar se é mobile
function checkMobile() {
  isMobile = window.innerWidth <= 992;
  if (isMobile) {
    document.body.style.overflow = 'auto';
    document.querySelector('.fullpage').style.transform = 'none';
  } else {
    document.body.style.overflow = 'hidden';
  }
}

// Inicialização de eventos
function initEvents() {
  // Evento de redimensionamento
  window.addEventListener('resize', debounce(function() {
    checkMobile();
    updateState();
  }, 250));

  // Eventos de scroll apenas para desktop
  if (!isMobile) {
    // Wheel event
    window.addEventListener('wheel', debounce(function(e) {
      if (isScrolling) return;
      if (e.deltaY > 0) changeSection(1);
      if (e.deltaY < 0) changeSection(-1);
    }, 50));

    // Touch events para desktop
    document.addEventListener('touchstart', function(e) {
      touchStartY = e.touches[0].clientY;
    });

    document.addEventListener('touchend', function(e) {
      touchEndY = e.changedTouches[0].clientY;
      handleTouchMove();
    });
  }

  // Eventos dos dots de navegação
  dots.forEach((dot, index) => {
    dot.addEventListener('click', () => goToSection(index));
  });

  // Evento de mudança de orientação
  window.addEventListener('orientationchange', function() {
    setTimeout(updateState, 200);
  });
}

// Função para lidar com movimento touch
function handleTouchMove() {
  if (isMobile) return;

  const diff = touchStartY - touchEndY;
  if (Math.abs(diff) > 50) { // Threshold mínimo
    if (diff > 0) changeSection(1);
    else changeSection(-1);
  }
}

// Função para mudar seção
function changeSection(direction) {
  if (isMobile) return;

  const newSection = currentSection + direction;
  if (newSection >= 0 && newSection < sections.length) {
    goToSection(newSection);
  }
}

// Função para ir para seção específica
function goToSection(index) {
  if (isMobile) return;

  isScrolling = true;
  currentSection = index;

  // Atualiza classes ativas
  sections.forEach(section => section.classList.remove('active'));
  dots.forEach(dot => dot.classList.remove('active'));

  sections[index].classList.add('active');
  dots[index].classList.add('active');

  // Aplica transformação
  const fullpage = document.querySelector('.fullpage');
  fullpage.style.transform = `translateY(-${index * 100}vh)`;

  // Reset do estado de scrolling
  setTimeout(() => {
    isScrolling = false;
  }, 1000);
}

// Função para atualizar estado
function updateState() {
  if (isMobile) {
    sections.forEach(section => {
      section.style.transform = 'none';
      section.style.transition = 'none';
    });
  } else {
    goToSection(currentSection);
  }
}

// Slideshow
let slideIndex = 1;
function initSlideshow() {
  showSlides(slideIndex);

  // Auto-play do slideshow
  setInterval(() => {
    if (!document.hidden) { // Verifica se a página está visível
      changeSlide(1);
    }
  }, 5000);
}

function changeSlide(n) {
  showSlides(slideIndex += n);
}

function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  const slides = document.getElementsByClassName("slide");
  const dots = document.getElementsByClassName("dot");

  if (n > slides.length) {slideIndex = 1}
  if (n < 1) {slideIndex = slides.length}

  for (let i = 0; i < slides.length; i++) {
    slides[i].style.display = "none";
    if (dots[i]) dots[i].classList.remove("active");
  }

  slides[slideIndex-1].style.display = "block";
  if (dots[slideIndex-1]) dots[slideIndex-1].classList.add("active");
}

// Utility function para debounce
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Modal
function openModal() {
  const modal = document.querySelector('.modal');
  if (modal) {
    modal.style.display = 'flex';
  }
}

function closeModal() {
  const modal = document.querySelector('.modal');
  if (modal) {
    modal.style.display = 'none';
  }
}

// Fecha modal ao clicar fora
window.onclick = function(event) {
  const modal = document.querySelector('.modal');
  if (event.target == modal) {
    closeModal();
  }
}