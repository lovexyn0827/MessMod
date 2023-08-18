package lovexyn0827.mess.util.access;

import java.util.LinkedList;
import java.util.regex.Matcher;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.phase.TickingPhaseArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;

public final class AccessingPathArgumentType implements ArgumentType<AccessingPath> {

	private AccessingPathArgumentType() {}

	public static AccessingPathArgumentType accessingPathArg() {
		return new AccessingPathArgumentType();
	}
	
	public static AccessingPath getAccessingPath(CommandContext<ServerCommandSource> ct, String string) {
		return ct.getArgument(string, AccessingPath.class);
	}
	
	/**
	 * Parse a string representation of {@link AccessingPath} into an instance.
	 * Example: !passengers.[0].getPos().y
	 * @throws CommandSyntaxException If the input is not a valid representation of an accessing path.
	 */
	@Override
	public AccessingPath parse(StringReader reader) throws CommandSyntaxException {
		LinkedList<Node> nodes = Lists.newLinkedList();
		String stringRepresentation = reader.getRemaining().trim();
		reader.setCursor(reader.getTotalLength());
		if(!stringRepresentation.endsWith(".")) {
			stringRepresentation += '.';
		}
		
		StringReader sr = new StringReader(stringRepresentation);
		Node n;
		int i = 0;
		while((n = readNode(sr)) != null) {
			if(!nodes.isEmpty() && !n.canFollow(nodes.getLast()) && OptionManager.strictAccessingPathParsing) {
				throw new TranslatableException("Node %s (%s) couldn't follow %s (%s)", 
						n.getClass().getSimpleName(), n, 
						nodes.getLast().getClass().getSimpleName(), nodes.getLast());
			}
			
			n.ordinary = i;
			nodes.add(n);
		}
		
		return new JavaAccessingPath(nodes, stringRepresentation);
	}

	@Nullable
	private Node readNode(StringReader sr) throws CommandSyntaxException {
		if(!sr.canRead()) {
			return null;
		}
		
		if(sr.peek() == '!') {
			sr.skip();
			String nodeStr = sr.readStringUntil('.');
			return new FieldNode(nodeStr);
		} else if(sr.peek() == '[') {
			//Element
			sr.skip();
			String nodeStr = sr.readStringUntil(']');
			sr.skip();
			try {
				return new ElementNode(Integer.parseInt(nodeStr));
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqint", nodeStr);
			}
		} else if(sr.peek() == '<') {
			//Map
			sr.skip();
			String nodeStr = sr.readStringUntil('>');
			sr.skip();
			return new ValueOfMapNode(Literal.parse(nodeStr));
		} else if(sr.peek() == '>') {
			sr.skip();
			String nodeStr = sr.readStringUntil('.');
			return new MapperNode(nodeStr);
		} else if(sr.peek() == '(') {
			sr.skip();
			String nodeStr = sr.readStringUntil(')');
			sr.skip();
			return new ClassCastNode(nodeStr);
		} else {
			String nodeStr = sr.readStringUntil('.');
			Matcher matcher = MethodNode.METHOD_PATTERN.matcher(nodeStr);
			if(matcher.matches()) {
				return new MethodNode(matcher.group("name"), matcher.group("types"), matcher.group("args"));
			} else {
				switch(nodeStr) {
				case "x" : 
					return new ComponentNode.X();
				case "y" : 
					return new ComponentNode.Y();
				case "z" : 
					return new ComponentNode.Z();
				case "identityHash" : 
					return SimpleNode.IDENTITY_HASH;
				case "size" : 
					return new SizeNode();
				default : 
					CustomNode node = CustomNode.create(nodeStr);
					if(node != null) {
						return node;
					} else {
						throw new TranslatableException("exp.unknownnode", nodeStr);
					}
				}
			}
		}
	}

	static {
		ArgumentTypes.register("mess_accessing_path", AccessingPathArgumentType.class, 
				new ConstantArgumentSerializer<AccessingPathArgumentType>(AccessingPathArgumentType::accessingPathArg));
	}
}
