package com.io.mekarobotics.definitions.robotics;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.content.perks.*;
import com.io.mekarobotics.common.content.perks.modules.PerkFist;
import com.io.mekarobotics.common.content.perks.modules.PerkGenerator;
import com.io.mekarobotics.common.content.perks.modules.PerkSolarPanel;
import com.io.mekarobotics.common.content.perks.modules.PerkUnarmedAttack;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ModPerks {

    public static final ResourceKey<Registry<Perk>> KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "perks"));
    public static final DeferredRegister<Perk> PERKS = DeferredRegister.create(KEY, Robotics.MODID);
    static {
        PERKS.makeRegistry(builder -> builder.defaultKey(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "undefined")).sync(false));
    }
    public static final Supplier<Registry<Perk>> REGISTRY = PERKS.getRegistry();

    public static final DeferredHolder<Perk, Perk> PERK_UNDEFINED = register("undefined", s -> new Perk(s, 1).setDisplayColor(ChatFormatting.RED));
    public static final DeferredHolder<Perk, Perk> PERK_HEALTH = register("health", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_ARMOR = register("durable", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_TOUGHNESS = register("persistent", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_MODULAR = register("modular", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_STEADY = register("steady", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_EFFICIENT = register("efficient", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_ACCELERATION = register("acceleration", PerkAcceleration::new);
    public static final DeferredHolder<Perk, Perk> PERK_CHARGE = register("charge", PerkCharge::new);
    public static final DeferredHolder<Perk, Perk> PERK_SOLAR_PANEL = register("solar_panel", PerkSolarPanel::new);
    public static final DeferredHolder<Perk, Perk> PERK_UNRELIABLE = register("unreliable", PerkUnreliable::new);
    public static final DeferredHolder<Perk, Perk> PERK_MASS_PRODUCED = register("mass_produced", PerkMassProduced::new);
    public static final DeferredHolder<Perk, Perk> PERK_IMPACT = register("impact", PerkImpact::new);
    public static final DeferredHolder<Perk, Perk> PERK_ROBUST = register("robust", PerkRobust::new);
    public static final DeferredHolder<Perk, Perk> PERK_LUMINOUS = register("luminous", Perk::new);
    //public static final DeferredHolder<Perk, Perk> PERK_GLOWING = register("glowing", PerkGlowing::new);
    public static final DeferredHolder<Perk, Perk> PERK_COAL_GENERATOR = registerGenerator("coal_generator",RoboticsConfig.general.coalGeneratorRate, (stack, ent) -> stack.getItem().equals(Items.COAL) ? 8000 : 0);
    public static final DeferredHolder<Perk, Perk> PERK_BIO_GENERATOR = registerGenerator("bio_generator", RoboticsConfig.general.bioGeneratorRate, (stack, ent) -> {
        FoodProperties stats = stack.getItem().getFoodProperties(stack, ent);
        if(stats == null) return 0;
        return (int) (stats.nutrition() * 200 + Math.pow(stats.saturation(), 1.5) * 100);
    });
    public static final DeferredHolder<Perk, Perk> PERK_UNARMED_ATTACK = register("unarmed_attack", PerkUnarmedAttack::new);
    public static final DeferredHolder<Perk, Perk> PERK_FIST = register("fist", PerkFist::new);
    public static final DeferredHolder<Perk, Perk> PERK_SHIELD = register("shield", Perk::new);

    public static final DeferredHolder<Perk, Perk> PERK_INVULNERABILITY = register("invulnerability", PerkInvulnerability::new);
    public static final DeferredHolder<Perk, Perk> PERK_VULNERABILITY = register("vulnerability", PerkVulnerability::new);
    public static final DeferredHolder<Perk, Perk> PERK_VOIDANT = register("voidant", PerkVoidant::new);
    public static final DeferredHolder<Perk, Perk> PERK_THERMAL_CONDUCTIVITY = register("thermal_conductivity", PerkThermalConductivity::new);
    public static final DeferredHolder<Perk, Perk> PERK_ELECTRICAL_CONDUCTIVITY = register("electrical_conductivity", PerkElectricalConductivity::new);
    public static final DeferredHolder<Perk, Perk> PERK_STACKED = register("stacked", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_LOGISTICIAN = register("logistician", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_REFLECTIVE = register("reflective", PerkReflective::new);
    public static final DeferredHolder<Perk, Perk> PERK_PRECIOUS = register("precious", PerkPrecious::new);
    public static final DeferredHolder<Perk, Perk> PERK_MAGNETIC = register("magnetic", PerkMagnetic::new);
    public static final DeferredHolder<Perk, Perk> PERK_CORRODABLE = register("corrodable", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_CONNEXIOM = register("connexiom", PerkConnexiom::new);
    public static final DeferredHolder<Perk, Perk> PERK_LINKED = register("linked", PerkLinked::new);
    public static final DeferredHolder<Perk, Perk> PERK_ATTRACTANT = register("attractant", PerkAttractant::new);
    public static final DeferredHolder<Perk, Perk> PERK_INSPIRE = register("inspire", PerkInspire::new);
    public static final DeferredHolder<Perk, Perk> PERK_MODULE_BUFF = register("module_buff", PerkModuleBuff::new);
    public static final DeferredHolder<Perk, Perk> PERK_MODULE_EFFICIENCY = register("module_efficiency", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_MODULE_COOLDOWN = register("module_cooldown", Perk::new);
    public static final DeferredHolder<Perk, Perk> PERK_ARMOR_SHRED = register("armor_shred", PerkArmorShred::new);

    private static DeferredHolder<Perk, Perk> registerGenerator(String name, Supplier<Integer> generationRate, BiFunction<ItemStack, Mob, Integer> validInputs) {
        return PERKS.register(name, () -> new PerkGenerator(name, generationRate, validInputs));
    }

    private static DeferredHolder<Perk, Perk> register(String name, Function<String, Perk> perk) {
        return PERKS.register(name, () -> perk.apply(name));
    }
}
