package ai.arcblroth.mixon;

import java.util.ListIterator;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

public class MixonPrePrePreLaunch {
    static {
        InstrumentationApi.retransform(ClassInfo.class, (final String name, final ClassNode klass) -> {
            for (final MethodNode method : klass.methods) {
                if (method.name.equals("hasSuperClass") && method.desc.equals("(Ljava/lang/String;Lorg/spongepowered/asm/mixin/transformer/ClassInfo$Traversal;)Z")) {
                    final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                    AbstractInsnNode node = iterator.next();

                    while (node != null) {
                        if (node.getOpcode() == Opcodes.IFEQ) {
                            final InsnList instructions = new InsnList();
                            final LabelNode compareSelf = new LabelNode();

                            instructions.add(new JumpInsnNode(Opcodes.IFEQ, compareSelf));
                            instructions.add(compareSelf);
                            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            instructions.add(new FieldInsnNode(Opcodes.GETFIELD, ClassInfo.class.getName().replace('.', '/'), "name", "Ljava/lang/String;"));
                            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));

                            method.instructions.insertBefore(node, instructions);

                            break;
                        }

                        node = iterator.next();
                    }
                }
            }
        });
    }
}
