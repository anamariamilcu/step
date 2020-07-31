/**
 * Shows different messages according to user log in status.
 */
function addLoginOrLogoutMessages() {
  const anchor = document.createElement('a');
  const initialMessage = document.createElement('span');
  const linkMessage = document.createElement('span');
  const container = document.getElementById('login-container');
  const form = document.getElementById('form-container');
  fetch('/login').then(response => response.json()).then((loginData) => {
    if (loginData.logStatus) {
      initialMessage.innerText = `Welcome, ${loginData.userEmail}!`;
      anchor.innerText = `Log out.`
      anchor.href = `${loginData.loginUrl}`;
      // Only show form when the blobstore url its's ready.
      fetchBlobstoreUrlAndShowForm(form);
    } else {
      initialMessage.innerText = 'Please log in to leave a comment';
      anchor.innerText = `Log in.`
      anchor.href = `${loginData.loginUrl}`;
    }
    linkMessage.appendChild(anchor);
    container.appendChild(initialMessage);
    container.appendChild(linkMessage);
    if (loginData.logStatus) {
      addNicknameLink(container);
    }
  });
}

/**
  * If the user is logged in, they can change or set up their nickname. 
  * So they can be redirected to the page for nickname set up.
  */
function addNicknameLink(container) {
  const br = document.createElement('br');
  const nicknameAnchor = document.createElement('a');
  const linkNickname = document.createElement('span');
  nicknameAnchor.innerText = 'Set nickname.'
  nicknameAnchor.href = 'nickname.html';
  container.appendChild(br);
  linkNickname.appendChild(nicknameAnchor);
  container.appendChild(linkNickname);
}

/**
  * On the form, the current nickname should be displayed if it does exist.
  * This way is easier for the user to edit it.
  */
function showCurrentNickname() {
  fetch('nickname').then(response => response.text()).then((nickname) => {
    document.getElementById('nickname').defaultValue = nickname;
  });
}

/**
  * This function will display the form only after the blobstore url has been
  * loaded from the server, until then, the user should not see it, since 
  * trying to load an image will generate an error.
  */
function fetchBlobstoreUrlAndShowForm(formElement) {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        formElement.action = imageUploadUrl;
        formElement.style.display = 'block';
      });
}
