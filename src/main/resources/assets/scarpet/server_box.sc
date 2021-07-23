__on_tick()->(
	for(entity_selector('@e[type=!player]'),
		pos = query(_,'pos');
		half_width = query(_,'width')/2;
		height = query(_,'height');
		draw_shape('box',1,
			'from',l((pos:0)-half_width,pos:1,(pos:2)-half_width),
			'to',l((pos:0)+half_width,(pos:1)+height,(pos:2)+half_width),
			'color',0x31f38b
		);
		draw_shape('line',1,'from',pos,'to',pos,'color',0x31f38b,'line',5);
		epos = [pos:0,(pos:1)+query(_,'eye_height'),pos:2];
		draw_shape('line',1,'from',epos,'to',epos,'color',0x31f38b,'line',5)
	)
)