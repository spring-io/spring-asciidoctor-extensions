var toggleGroupNames = new Array();

function getGroupNames() {
  // The replace call gets rid of return characters.
  toggleGroupNames = document.getElementsByClassName('toggle-group-names')[0].innerText.replace(/(\r\n|\n|\r)/gm, "").split(',');
}

function addToggleButtons() {
  var contentElement = document.getElementById("content");
  var preambleElement = document.getElementById("preamble");
  var toggleGroupContainer = document.createElement("div");
  toggleGroupContainer.className = "toggle-group-container";
  toggleGroupContainer.id = "toggle-group-container";
  contentElement.insertBefore(toggleGroupContainer, preambleElement);
  for (var i = 0; i < toggleGroupNames.length; i++) {
    var toggleContainer = document.createElement("div");
    toggleContainer.className = "toggle-container";
    var buttons = createToggleButtons(toggleGroupNames[i]);
    for (var j = 0; j < buttons.length; j++) {
      toggleContainer.appendChild(buttons[j]);
    }
    toggleGroupContainer.appendChild(toggleContainer);
  }
}

function createToggleButtons(toggleGroupValues) {
  var labels = new Array();
  var buttons = new Array();
  labels = toggleGroupValues.split('-');
  for (var i = 0; i < labels.length; i++) {
    var buttonId = toggleGroupValues + "-group-" + labels[i] + "-button";
    var button = document.createElement("button");
    button.id = buttonId;
    button.innerHTML = labels[i];
    buttons.push(button);
  }
  return buttons;
}

function createEventListener() {
  document.getElementById("toggle-group-container").addEventListener("click", function(e) {
    if(e.target && e.target.nodeName == "BUTTON") {
      // 1. Get the group name from the button container.
      var groupName = "group-name:" + e.target.id.substring(0, e.target.id.indexOf("-group"));
      // 2. Get the button value.
      var buttonValue = "group-value:" + e.target.innerHTML.toLowerCase();
      // 3. For each element in the document with that group name, hide it.
      // Doing it this way supports an arbitrary number of buttons.
      var elementsToHide = document.getElementsByClassName(groupName);
      for (var i = 0; i < elementsToHide.length; i++) {
        elementsToHide[i].style.display = "none";
      }
      // 4. For each element in the document with that group name and button value, show it.
      var elementsToShow = document.getElementsByClassName(groupName + " " + buttonValue);
      for (var i = 0; i < elementsToShow.length; i++) {
        elementsToShow[i].style.display = "block";
      }
      // 5. Make the buttons be toggles.
      var buttonElements = e.target.parentElement.children;
      for (var i = 0; i < buttonElements.length; i++) {
        if (buttonElements[i].innerHTML === e.target.innerHTML) {
          buttonElements[i].classList.add("pressed");
        } else {
          buttonElements[i].classList.remove("pressed");
        }
      }
      // 6. Save the choice to localStorage
      localStorage.setItem(groupName, e.target.innerHTML);
    }
  });
}

function setInitialValues() {
  for (var key in localStorage){
    if (key.indexOf('group-name:') > -1) {
      var keyValue = localStorage.getItem(key);
      var buttonId = key.substring(11, key.length) + "-group-" + keyValue + "-button";
      document.getElementById(buttonId).classList.add("pressed");
    }
  }
}

$(getGroupNames);
$(addToggleButtons);
$(createEventListener);
$(setInitialValues);
