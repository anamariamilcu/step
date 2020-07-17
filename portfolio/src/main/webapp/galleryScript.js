function openGallery() {
	document.getElementById('myGallery').style.display = 'block';
}

function closeGallery() {
	document.getElementById('myGallery').style.display = 'none';
}

let slideIndex = 1;
showSlidingGallery(slideIndex);

function moveNSlides(n) {
	showSlidingGallery(slideIndex += n);
}

function showCurrentSlide(n) {
	showSlidingGallery(slideIndex = n);
}

function showSlidingGallery(n) {
	let i;
	const slides = document.getElementsByClassName('slide');
	const descriptionText = document.getElementById('description');
  const slideNumber = document.getElementById('slide-number');
	if (n > slides.length) {
		slideIndex = 1;
	} else if (n < 1) {
		slideIndex = slides.length;
	}
	for (i = 0; i < slides.length; i++) {
		slides[i].style.display = 'none';
	}
	slides[slideIndex - 1].style.display = 'block';
  descriptionText.style.fontSize = 'x-large';
	descriptionText.innerHTML = slides[slideIndex - 1].getElementsByTagName('img')[0].alt;
  slideNumber.innerHTML = slideIndex + '/' + slides.length;
}
