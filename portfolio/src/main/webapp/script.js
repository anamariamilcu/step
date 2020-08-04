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
  // Sets the url of the image I want to add to the page.
  imgElement.src = 'images/friends.jpg';
  imgElement.width = '600';

  const imageContainer = document.getElementById('friends-image-container');
  // Remove the previous image.
  imageContainer.innerHTML = '';
  imageContainer.appendChild(imgElement);
}

function getCommentSectionFromServer() {
  const commentsNumber = document.getElementById('comment-number').value;
  const commentsOrder = document.getElementById('comment-order').value;
  const url = `/comment-section?comments_number=${commentsNumber}&comments_order=${commentsOrder}`;
  fetch(url).then(response => response.json()).then((comments) => {
    const commSection = document.getElementById('comment-list');
    // Remove the comments that already existed.
    commSection.innerHTML = "";
    // Build the comment section.
    comments.forEach(comm => {
      commSection.appendChild(createIndividualComment(comm));
    });
  });
}

// Format each particular comment to show up on page.
function createIndividualComment(comment) {
  const liElement = document.createElement('li');
  liElement.setAttribute('class', 'comment-li');
  // This container is used for showing the username.
  const usernameElement = document.createElement('div');
  usernameElement.setAttribute('class', 'username-container');
  // This container is used for showing the text of the comment.
  const commentElement = document.createElement('div');
  commentElement.setAttribute('class', 'comment-text');
  // Made an image represent the delete button.
  const deleteButtonElement = document.createElement('img');
  deleteButtonElement.setAttribute('class', 'delete-button');
  deleteButtonElement.src = 'images/trash.png';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment, liElement);
  });
  
  usernameElement.innerText = `${comment.username} wrote on ${comment.date}`;
  commentElement.innerText = comment.text;

  liElement.appendChild(deleteButtonElement);
  liElement.appendChild(usernameElement);
  liElement.appendChild(commentElement);

  /* If there was any image attachment. */
  if (typeof comment.blobKeyString !== 'undefined') {
    liElement.appendChild(addUploadedImageToComment(comment));
    liElement.appendChild(addImageLabels(comment.imageLabels));
  }
  
  return liElement;
}

function addUploadedImageToComment(comment) {
  const imageElement = document.createElement('img');
  imageElement.setAttribute('class', 'comment-image');
  imageElement.src = `/serve?blob-key=${comment.blobKeyString}`;
  const imageAnchor = document.createElement('a');
  imageAnchor.href = `/serve?blob-key=${comment.blobKeyString}`;
  imageAnchor.appendChild(imageElement);
  return imageAnchor;
}

function addImageLabels(labels) {
  const ulElement = document.createElement('ul');
  ulElement.setAttribute('class', 'comment-labels');
  labels.forEach(label => {
    const labelElement = document.createElement('li');
    const labelText = document.createTextNode(label);
    labelElement.appendChild(labelText);
    ulElement.appendChild(labelElement);
  });
  return ulElement;
}

/* Deletes the comment entity from the datastore with DELETE request
and removes the container element if it was deleted successfully. */
function deleteComment(comment, element) {
  let url = `/delete-comment?id=${comment.id}`;
  fetch(url, {method: 'DELETE'}).then(result => {
    //Check if the comment was deleted succesfully.
    if (result.status === 200) {
      // Remove the comment container only if the request was succesfull.
      element.remove();
    }
    //TODO handle the possible error.
  });
}
