package lovexyn0827.mess.util.access;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;

import lovexyn0827.mess.options.OptionManager;

class PathCompiler {
	private final LinkedList<Node> nodes = new LinkedList<>();
	private final CompilationContext ctx;
	private ClassNode classFile;
	/** 
	 * The internal name of the class of this compiled accessing path
	 */
	private String className;
	private InsnList clinitInsns;
	
	public PathCompiler(List<Node> compilers, List<Class<?>> nodeInputTypes, String name) {
		compilers.forEach(this.nodes::add);
		this.ctx = new CompilationContext(nodeInputTypes, name);
		this.className = this.ctx.getInternalClassNameOfPath();
	}

	public Class<?> compile() throws CompilationException {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		ClassVisitor wrappedCw;
		if(OptionManager.superSuperSecretSetting) {
			wrappedCw = new TraceClassVisitor(cw, new PrintWriter(System.out));
		} else {
			wrappedCw = cw;
		}
		
		this.classFile = new ClassNode();
		// 1. Build class metadata
		this.classFile.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, 
				this.className, null, 
				org.objectweb.asm.Type.getInternalName(CompiledPath.class), 
				new String[] {});
		// 2. Add default constructor
		MethodNode initMethod = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, 
				"<init>", "()V", null, new String[] {});
		InsnList initInsns = initMethod.instructions;
		initInsns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		initInsns.add(new LdcInsnNode(this.ctx.getOriginalName()));
		initInsns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, 
				org.objectweb.asm.Type.getInternalName(CompiledPath.class), "<init>", "(Ljava/lang/String;)V"));
		initInsns.add(new InsnNode(Opcodes.RETURN));
		this.classFile.methods.add(initMethod);
		// 3. Build access method & compile nodes
		this.buildAccessMethod();
		// 4. Create class initializer
		MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, 
				"<clinit>", "()V", null, new String[] {});
		this.classFile.methods.add(clinit);
		this.clinitInsns = clinit.instructions;
		// 5. Prepare constants
//		this.buildConstantCountSupplier();
		this.prepareStaticConstants();
		this.prepareLambdas();
		this.prepareSubPaths();
		this.prepareDynamicLiterals();
		// 6. Finish class initializer creation
		this.clinitInsns.add(new InsnNode(Opcodes.RETURN));
		// 7. Replace uncertain field descriptors
		Map<String, FieldNode> fieldsByName = new HashMap<>();
		this.classFile.fields.forEach((fn) -> fieldsByName.put(fn.name, fn));
		for(MethodNode mn : this.classFile.methods) {
			for(AbstractInsnNode insn : mn.instructions) {
				if(insn instanceof FieldInsnNode) {
					FieldInsnNode finsn = (FieldInsnNode) insn;
					if(finsn.desc == BytecodeHelper.UNCERTAIN_FIELD_DESCRIPTOR && finsn.owner == this.className) {
						finsn.desc = fieldsByName.get(finsn.name).desc;
					}
				}
			}
		}
		
		// 8. Generate and load class
		this.classFile.accept(wrappedCw);
		byte[] clBytes = cw.toByteArray();
		try {
			return PathClassLoader.INSTANCE.defineClass(this.className.replace('/', '.'), clBytes);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}

	private void prepareDynamicLiterals() {
		List<Literal<?>> literals = this.ctx.getDynamicLiterals();
		int count = literals.size();
		String desc = '[' + org.objectweb.asm.Type.getDescriptor(Literal.class);
		FieldNode fn = new FieldNode(
				Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, 
				"DYNAMIC_LITERALS", desc, null, null);
		this.classFile.fields.add(fn);
		InsnList insns = this.clinitInsns;
		insns.add(new IntInsnNode(Opcodes.BIPUSH, count));
		insns.add(new TypeInsnNode(Opcodes.ANEWARRAY, org.objectweb.asm.Type.getInternalName(Literal.class)));
		insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, this.className, "DYNAMIC_LITERALS", desc));
		for(int i = 0; i < count; i++) {
			insns.add(new FieldInsnNode(Opcodes.GETSTATIC, this.className, "DYNAMIC_LITERALS", desc));
			insns.add(new IntInsnNode(Opcodes.BIPUSH, i));
			insns.add(new LdcInsnNode(literals.get(i).serialize()));
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Literal.class), 
					"parse", "(Ljava/lang/String;)Llovexyn0827/mess/util/access/Literal;"));
			insns.add(new InsnNode(Opcodes.AASTORE));
		}
	}

	private void prepareSubPaths() {
		List<AccessingPath> paths = this.ctx.getSubPaths();
		int count = paths.size();
		String desc = '[' + org.objectweb.asm.Type.getDescriptor(AccessingPath.class);
		FieldNode fn = new FieldNode(
				Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, 
				"SUBPATHS", desc, null, null);
		this.classFile.fields.add(fn);
		InsnList insns = this.clinitInsns;
		insns.add(new IntInsnNode(Opcodes.BIPUSH, count));
		insns.add(new TypeInsnNode(Opcodes.ANEWARRAY, org.objectweb.asm.Type.getInternalName(AccessingPath.class)));
		insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, this.className, "SUBPATHS", desc));
		for(int i = 0; i < count; i++) {
			insns.add(new FieldInsnNode(Opcodes.GETSTATIC, this.className, "SUBPATHS", desc));
			insns.add(new IntInsnNode(Opcodes.BIPUSH, i));
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(AccessingPathArgumentType.class), 
					"accessingPathArg", "()" + org.objectweb.asm.Type.getDescriptor(AccessingPathArgumentType.class)));
			insns.add(new TypeInsnNode(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(StringReader.class)));
			insns.add(new InsnNode(Opcodes.DUP));
			insns.add(new LdcInsnNode(paths.get(i).getOriginalStringRepresentation()));
			insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, 
					org.objectweb.asm.Type.getInternalName(StringReader.class), 
					"<init>", "(Ljava/lang/String;)V"));
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(AccessingPathArgumentType.class), 
					"parse", org.objectweb.asm.Type.getMethodDescriptor(
							org.objectweb.asm.Type.getType(AccessingPath.class), 
							org.objectweb.asm.Type.getType(StringReader.class))));
			insns.add(new InsnNode(Opcodes.AASTORE));
		}
	}

	private void prepareLambdas() throws CompilationException {
		// 1. Serialize lambdas
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(ObjectOutputStream oos = new ObjectOutputStream(baos)){
			oos.writeInt(this.ctx.getLambdas().size());
			for(Function<?, ?> func : this.ctx.getLambdas()) {
				oos.writeObject(func);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new CompilationException(FailureCause.ERROR, e.getLocalizedMessage());
		}
		
		byte[] bytes = baos.toByteArray();
		// 2. Write annotation
		AnnotationNode an = new AnnotationNode(org.objectweb.asm.Type.getDescriptor(CompiledPath.Lambdas.class));
		an.values = Lists.newArrayList("bytes", bytes);
		this.classFile.visibleAnnotations = Lists.newArrayList(an);
		// 3. Generate deserializer
		FieldNode fn = new FieldNode(
				Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, 
				"LAMBDAS", "[Ljava/util/function/Function;", null, null);
		this.classFile.fields.add(fn);
		this.clinitInsns.add(new LdcInsnNode(org.objectweb.asm.Type.getType('L' + this.className + ';')));
		this.clinitInsns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
				org.objectweb.asm.Type.getInternalName(CompiledPath.class), 
				"parseLambdas", "(Ljava/lang/Class;)[Ljava/util/function/Function;"));
		this.clinitInsns.add(new FieldInsnNode(Opcodes.PUTSTATIC, this.className, 
				"LAMBDAS", "[Ljava/util/function/Function;"));
	}

	private void prepareStaticConstants() throws CompilationException {
		InsnList arrayCreation = new InsnList();
		InsnList cstLoading = new InsnList();
		this.ctx.getStaticLiterals().forEach((cl, listWrapper) -> {
			// Create and initialize field
			String fieldName = "SC$" + listWrapper.getFirst();
			List<Literal<?>> list = listWrapper.getSecond();
			int cstCountInType = list.size();
			String fieldType = "[" + org.objectweb.asm.Type.getDescriptor(cl);
			FieldNode fn = new FieldNode(
					Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, 
					fieldName, fieldType, null, null);
			this.classFile.fields.add(fn);
			arrayCreation.add(new IntInsnNode(Opcodes.BIPUSH, cstCountInType));
			arrayCreation.add(new TypeInsnNode(Opcodes.ANEWARRAY, org.objectweb.asm.Type.getInternalName(cl)));
			arrayCreation.add(new FieldInsnNode(Opcodes.PUTSTATIC, this.className, fieldName, fieldType));
			// Load constants to the arrays
			for(int i = 0; i < cstCountInType; i++) {
				cstLoading.add(new FieldInsnNode(Opcodes.GETSTATIC, this.className, fieldName, fieldType));
				cstLoading.add(new IntInsnNode(Opcodes.BIPUSH, i));
				cstLoading.add(new LdcInsnNode(list.get(i).stringRepresentation));
				cstLoading.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
						org.objectweb.asm.Type.getInternalName(Literal.class), 
						"parse", "(Ljava/lang/String;)Llovexyn0827/mess/util/access/Literal;"));
				cstLoading.add(new LdcInsnNode(org.objectweb.asm.Type.getType(cl)));
				cstLoading.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
						org.objectweb.asm.Type.getInternalName(Literal.class), 
						"get", "(Ljava/lang/reflect/Type;)Ljava/lang/Object;"));
				cstLoading.add(new TypeInsnNode(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(cl)));
				cstLoading.add(new InsnNode(Opcodes.AASTORE));
			}
		});
		this.clinitInsns.add(arrayCreation);
		this.clinitInsns.add(cstLoading);
	}

	private void buildAccessMethod() throws CompilationException {
		MethodNode accessMethod = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, 
				"access", "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Ljava/lang/Object;", null, 
				new String[] {org.objectweb.asm.Type.getInternalName(AccessingFailureException.class)});
		int i = 0;
		InsnList accessInsns = accessMethod.instructions;
		accessInsns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		accessInsns.add(new TypeInsnNode(Opcodes.CHECKCAST, 
				org.objectweb.asm.Type.getInternalName(this.ctx.getInputClassOverrideAt(0))));
		for(Node node : this.nodes) {
			Class<?> lastOutputCl = this.ctx.getLastOutputClass();
			if(lastOutputCl.isPrimitive() && !node.allowsPrimitiveTypes()) {
				BytecodeHelper.appendPrimitiveWrapper(accessInsns, lastOutputCl);
			}
			
			try {
				node.initialize(this.ctx.getLastOutputType());
			} catch (AccessingFailureException e) {
				throw new CompilationException(e.failureCause, e.args);
			}
			
			InsnList insns = node.getCompiler().compile(this.ctx);
			LabelNode l = new LabelNode();
			accessInsns.add(l);
			accessInsns.add(new LineNumberNode(i++, l));
			accessInsns.add(insns);
		}
		
		Class<?> finalType = this.ctx.getLastOutputClass();
		if(finalType == void.class) {
			accessInsns.add(new InsnNode(Opcodes.ACONST_NULL));
		} else if(finalType.isPrimitive()) {
			BytecodeHelper.appendPrimitiveWrapper(accessInsns, finalType);
		}
		
		accessInsns.add(new InsnNode(Opcodes.ARETURN));
		this.classFile.methods.add(accessMethod);
		this.ctx.lockConstantLists();
	}
}
