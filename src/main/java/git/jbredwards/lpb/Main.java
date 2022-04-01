package git.jbredwards.lpb;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Loading Progress Bar Plugin")
@Mod(modid = "lpb", name = "Loading Progress Bar", version = "1.0", clientSideOnly = true)
public final class Main implements IFMLLoadingPlugin
{
    private static boolean obfuscated;

    public static final class Transformer implements IClassTransformer
    {
        @Override
        public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
            if("net.minecraft.client.LoadingScreenRenderer".equals(transformedName) || "net.minecraft.server.MinecraftServer".equals(transformedName)) {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                //does the changes
                methods:
                for(MethodNode method : classNode.methods) {
                    //fix client display
                    if((obfuscated ? "func_73719_c" : "displayLoadingString").equals(method.name)) {
                        for(AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == Opcodes.ICONST_M1) {
                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/lpb/Main", "get", "()I"));
                                method.instructions.remove(insn);
                                break methods;
                            }
                        }
                    }
                    //sync progress from server
                    else if((obfuscated ? "func_71222_d" : "initialWorldChunkLoad").equals(method.name)) {
                        for(AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == Opcodes.IINC) {
                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/lpb/Main", "set", "(I)V"));
                                method.instructions.insert(insn, new InsnNode(Opcodes.IDIV));
                                method.instructions.insert(insn, new IntInsnNode(Opcodes.SIPUSH, 625));
                                method.instructions.insert(insn, new InsnNode(Opcodes.IMUL));
                                method.instructions.insert(insn, new IntInsnNode(Opcodes.BIPUSH, 100));
                                method.instructions.insert(insn, new VarInsnNode(Opcodes.ILOAD, 5));
                                break;
                            }
                        }
                    }
                    //reset progress
                    else if((obfuscated ? "func_71243_i" : "clearCurrentTask").equals(method.name)) {
                        for(AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == Opcodes.ICONST_0) {
                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/lpb/Main", "set", "(I)V"));
                                method.instructions.insert(insn, new InsnNode(Opcodes.ICONST_M1));
                                if(method.maxStack < 3) method.maxStack++;
                                break methods;
                            }
                        }
                    }
                }
                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                //returns the transformed class
                return writer.toByteArray();
            }

            return basicClass;
        }
    }

    private static int progress = -1;
    public static synchronized int get() { return progress; }
    public static synchronized void set(int progressIn) { progress = progressIn; }

    @Nonnull
    @Override
    public String[] getASMTransformerClass() { return new String[] { "git.jbredwards.lpb.Main$Transformer" }; }

    @Override
    public void injectData(@Nonnull Map<String, Object> data) { obfuscated = (boolean)data.get("runtimeDeobfuscationEnabled"); }

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
