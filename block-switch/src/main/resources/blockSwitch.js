function addBlockSwitches() {
	for (var primary of document.querySelectorAll('.primary')) {
		var switchItem = createSwitchItem(primary, createBlockSwitch(primary));
		switchItem.item.classList.add("selected");
		var title = primary.querySelector('.title')
		title.remove();
	}
	for (var secondary of document.querySelectorAll('.secondary')) {
		var primary = findPrimary(secondary);
		if (primary === null) {
			console.error("Found secondary block with no primary sibling");
		}
		else {
			var switchItem = createSwitchItem(secondary, primary.querySelector('.switch'));
			switchItem.content.classList.add("hidden");
			primary.append(switchItem.content);
			secondary.remove();
		}
	}
}

function createElementFromHtml(html) {
	var template = document.createElement('template');
    template.innerHTML = html;
    return template.content.firstChild;
}

function createBlockSwitch(primary) {
    var blockSwitch = createElementFromHtml('<div class="switch"></div>');
    primary.prepend(blockSwitch)
	return blockSwitch;
}

function findPrimary(secondary) {
	var candidate = secondary.previousElementSibling;
	while (candidate != null && !candidate.classList.contains('primary')) {
		candidate = candidate.previousElementSibling;
	}
	return candidate;
}

function createSwitchItem(block, blockSwitch) {
	var blockName = block.querySelector('.title').textContent;
	var content = block.querySelectorAll('.content').item(0);
	var colist = nextSibling(block, '.colist');
	if (colist != null) {
		content.append(colist);
	}
	var item = createElementFromHtml('<div class="switch--item">' + blockName + '</div>');
	item.dataset.blockName = blockName;
	content.dataset.blockName = blockName;
	blockSwitch.append(item);
	return {'item': item, 'content': content};
}

function nextSibling(element, selector) {
	var sibling = element.nextElementSibling;
	while (sibling) {
		if (sibling.matches(selector)) {
			return sibling;
		}
		sibling = sibling.nextElementSibling;
	}
}

function globalSwitch() {
	document.querySelectorAll(".switch--item").forEach(function(item) {
		var blockId = blockIdForSwitchItem(item);
		var handler = function(event) {
			selectedText = event.target.textContent;
			window.localStorage.setItem(blockId, selectedText);
			for (var switchItem of document.querySelectorAll(".switch--item")) {
				if (blockIdForSwitchItem(switchItem) === blockId && switchItem.textContent === selectedText) {
					select(switchItem);
				}
			}
		}
		item.addEventListener("click", handler);
		if (item.textContent === window.localStorage.getItem(blockId)) {
			select(item);
		}
	});
}

function select(selected) {
	for (var child of selected.parentNode.children) {
		child.classList.remove("selected");
	}
	selected.classList.add("selected");
	for (var child of selected.parentNode.parentNode.children) {
		if (child.classList.contains("content")) {
			if (selected.dataset.blockName === child.dataset.blockName) {
				child.classList.remove("hidden");
			}
			else {
				child.classList.add("hidden");
			}
		}
	}	
}

function blockIdForSwitchItem(item) {
	idComponents = []
	for (var switchItem of item.parentNode.querySelectorAll(".switch--item")) {
		idComponents.push(switchItem.textContent.toLowerCase());
	}
	return idComponents.sort().join("-")
}

window.onload = function() {
	addBlockSwitches();
	globalSwitch();
};
