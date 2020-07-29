/**
 * Shows different messages according to user log in status.
 */
function addLoginOrLogoutMessages() {
  const anchor = document.createElement('a');
  const initialMessage = document.createElement('span');
  const linkMessage = document.createElement('span');
  const container = document.getElementById('login-container');
  fetch('/login').then(response => response.json()).then((loginData) => {
    if (loginData.logStatus) {
      initialMessage.innerText = `Welcome, ${loginData.userEmail}!`;
      anchor.innerText = `Log out.`
      anchor.href = `${loginData.loginUrl}`;
    } else {
      initialMessage.innerText = 'Please log in to leave a comment';
      anchor.innerText = `Log in.`
      anchor.href = `${loginData.loginUrl}`;
      const form = document.getElementById('form-container');
      form.style.display = 'none';
    }
    linkMessage.appendChild(anchor);
    container.appendChild(initialMessage);
    container.appendChild(linkMessage);
  });
}