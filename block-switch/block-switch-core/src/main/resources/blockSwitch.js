// var blockSwitchGroupNames = ["javakotlin", "mavengradle"];
var blockSwitchGroupNames = new Array();

function addBlockSwitches() {
	$('.primary').each(function() {
		primary = $(this);
		createSwitchItem(primary, createBlockSwitch(primary));
		primary.children('.title').remove();
	});
	$('.secondary').each(function(idx, node) {
		secondary = $(node);
		primary = findPrimary(secondary);
		switchItem = createSwitchItem(secondary, primary.children('.switch'));
		switchItem.content.addClass('hidden');
		findPrimary(secondary).append(switchItem.content);
		secondary.remove();
		var groupName = primary.find('div.switch--item').text().toLowerCase();
		if (!blockSwitchGroupNames.includes(groupName)) {
			blockSwitchGroupNames.push(groupName);
		}
		var nameValue = $(this).children('div.title').text().toLowerCase();
		var blockSwitchGroupName = "";
		for (var i = 0; i < blockSwitchGroupNames.length; i++) {
			var namesLength = blockSwitchGroupNames.length;
			var currentName = blockSwitchGroupNames[i];
			if (currentName.includes(nameValue)) {
				blockSwitchGroupName = currentName;
				break;
			}
		}
		if (window.localStorage.getItem(blockSwitchGroupName) === null) {
			window.localStorage.setItem(blockSwitchGroupName, $(this).find('div.switch--item').text().toLowerCase());
		}
		var thisText = $(this).text();
		$(".switch--item:lowerContains(" + window.localStorage.getItem(blockSwitchGroupName) +")").addClass("selected");
	});
}

jQuery.expr[':'].lowerContains = function(a, i, m) {
  return jQuery(a).text().toLowerCase()
      .indexOf(m[3].toLowerCase()) >= 0;
};

function createBlockSwitch(primary) {
	blockSwitch = $('<div class="switch"></div>');
	primary.prepend(blockSwitch);
	return blockSwitch;
}

function findPrimary(secondary) {
	candidate = secondary.prev();
	while (!candidate.is('.primary')) {
		candidate = candidate.prev();
	}
	return candidate;
}

function createSwitchItem(block, blockSwitch) {
	blockName = block.children('.title').text();
	content = block.children('.content').first().append(block.next('.colist'));
	item = $('<div class="switch--item">' + blockName + '</div>');
	item.on('click', '', content, function(e) {
		$(this).addClass('selected');
		$(this).siblings().removeClass('selected');
		e.data.siblings('.content').addClass('hidden');
		e.data.removeClass('hidden');
	});
	blockSwitch.append(item);
	return {'item': item, 'content': content};
}

function globalSwitch() {
	$('.switch--item').each(function() {
		$(this).off('click');
		$(this).on('click', function() {
			var switchValue = $(this).text().toLowerCase();
			var blockSwitchGroupName = "";
			for (var i = 0; i < blockSwitchGroupNames.length; i++) {
				if (blockSwitchGroupNames[i].includes(switchValue)) {
					blockSwitchGroupName = blockSwitchGroupNames[i];
					break;
				}
			}
			window.localStorage.setItem(blockSwitchGroupName, switchValue);
			selectedText = $(this).text()
			selectedIndex = $(this).index()
			$(".switch--item").filter(function() { return ($(this).text() === selectedText) }).each(function() {
				$(this).addClass('selected');
				$(this).siblings().removeClass('selected');
				selectedContent = $(this).parent().siblings(".content").eq(selectedIndex)
				selectedContent.removeClass('hidden');
				selectedContent.siblings().addClass('hidden');
			});
		});
	});
}

$(addBlockSwitches);
$(globalSwitch);
