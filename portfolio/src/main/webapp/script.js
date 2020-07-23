// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random fact to the page.
 */

function addAndReplaceRandomFact() {
  const facts = [
    'I don\'t drink coffee',
    'My birthday is on 29th of May',
    'I love vanilla ice cream',
    'My favourite author is Jane Austen',
    'I love ice-skating',
  ];

  // Pick a random fact.
  const chosenFact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = chosenFact;
}

/**
 * When a button is clicked, this function adds a photo on the page.
 */

function addPhoto() {
  const imgElement = document.createElement('img');
  // Sets the url of the image I want to add to the page
  imgElement.src = 'images/friends.jpg';
  imgElement.width = '600';

  const imageContainer = document.getElementById('friends-image-container');
  // Remove the previous image.
  imageContainer.innerHTML = '';
  imageContainer.appendChild(imgElement);
}

function getCommentSectionFromServer() {
  fetch('/comment-section').then(response => response.json()).then((comments) => {
    console.log(comments);
    const commSection = document.getElementById('comment-list');
    // Build the comment section.
    comments.forEach((comm) => {
      commSection.appendChild(createIndividualComment(comm));
      console.log(comm);
    });
  });
}

// Format each particular comment to show up on page.
function createIndividualComment(comment) {
  const liElement = document.createElement('li');
  const usernameElement = document.createElement('p');
  const commentElement = document.createElement('span');
  usernameElement.innerText = comment.username + ' wrote:';
  commentElement.innerText = comment.text;
  commentElement.style.fontSize = 'initial';
  commentElement.style.fontStyle = 'italic';
  commentElement.style.fontWeight = 'initial';
  commentElement.style.height = '100%';
  liElement.appendChild(usernameElement);
  liElement.appendChild(commentElement);
  liElement.style.border = 'dotted pink';
  return liElement;
}
