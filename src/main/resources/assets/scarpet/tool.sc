__on_player_uses_item(player,item,h)->(
	if(item:0=='brick',
		run('tick freeze');
		return()
	);
	if(item:0=='bone',
		run('tick step '+item:1);
		return()
	);
	if(item:0=='netherite_ingot',
		for(entity_selector('@e[type=!player]'),
			modify(_,'remove')
		);
		return()
	);
)