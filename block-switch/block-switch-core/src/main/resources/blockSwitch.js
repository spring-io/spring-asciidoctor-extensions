function addBlockSwitches() {
	$('.primary').each(function() {
		primary = $(this);
		createSwitchItem(primary, createBlockSwitch(primary)).item.addClass("selected");
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
	blockSwitch.append(item);
	return {'item': item, 'content': content};
}

function globalSwitch() {
	$('.switch--item').each(function() {
		blockId = blockIdForSwitchItem($(this));
		$(this).off('click');
		$(this).on('click', function() {
			selectedText = $(this).text()
			window.localStorage.setItem(blockId, selectedText);
			$(".switch--item").filter(function() {
				return blockIdForSwitchItem($(this)) === blockId;
			}).filter(function() {
				return $(this).text() === selectedText;
			}).each(function() {
				select($(this))
			});
		});
		if ($(this).text() === window.localStorage.getItem(blockId)) {
			select($(this))
		}
	});
}

function blockIdForSwitchItem(item) {
	idComponents = []
	idComponents.push(item.text().toLowerCase());
	item.siblings(".switch--item").each(function(index, sibling) {
		idComponents.push($(sibling).text().toLowerCase());
	});
	return idComponents.sort().join("-")
}

function select(selected) {
	selected.addClass('selected');
	selected.siblings().removeClass('selected');
	selectedContent = selected.parent().siblings(".content").eq(selected.index())
	selectedContent.removeClass('hidden');
	selectedContent.siblings().addClass('hidden');
}

$(addBlockSwitches);
$(globalSwitch);
