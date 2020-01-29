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
	});
	if (window.localStorage.getItem("blockSwitch") === null) {
		window.localStorage.setItem("blockSwitch", $( "div.primary" ).find("div.switch--item").first().text())
	}
	$(".switch--item:contains(" + window.localStorage.getItem("blockSwitch") +")").addClass("selected");
}

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
			window.localStorage.setItem("blockSwitch", $(this).text());
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
