function openGallery() {
	document.getElementById('myGallery').style.display = 'block';
}

function closeGallery() {
	document.getElementById('myGallery').style.display = 'none';
}

var slideIndex = 1;
showSlides(slideIndex);

function moveNSlides(n) {
	showSlides(slideIndex += n);
}

function showCurrentSlide(n) {
	showSlides(slideIndex = n);
}

function showSlides(n) {
	var i;
	var slides = document.getElementsByClassName('slide');
	var descriptionText = document.getElementById('description');
    var slideNumber = document.getElementById('slide-number');
	if (n > slides.length) {
		slideIndex = 1;
	}
	if (n < 1) {
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
