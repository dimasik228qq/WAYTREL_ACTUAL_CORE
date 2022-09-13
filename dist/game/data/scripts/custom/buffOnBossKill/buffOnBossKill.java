package custom.buffOnBossKill;

import ai.AbstractNpcAI;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Index
 */
public class buffOnBossKill extends AbstractNpcAI
{
    private static final int[] RAID_BOSSES =
            {
                    // 65
                    25102, // Shacram
                    25122, // Refugee Applicant Leo
                    25226, // Roaring Lord Kastor
                    25420, // Orfen's Handmaiden
                    25749, // Tayga Feron King
                    25750, // Tayga Marga Shaman
                    25751, // Tayga Septon Champion
                    // 70
                    25004, // Turek Mercenary Captain
                    25230, // Priest Ragoth
                    25256, // High Prefect Arak
                    25431, // Flamestone Golem
                    25463, // Guardian Garangky
                    25747, // Rael Mahum Radium
                    25748, // Rael Mahum Supercium
                    25754, // Flamestone Giant
                    // 75
                    25026, // Atis
                    25044, // Barion
                    25051, // Rahha
                    25095, // Renoa
                    25099, // Repiro
                    25146, // Cursed Bifrons
                    25152, // Cursed Shadir
                    25155, // Selu
                    25217, // Cursed Kele
                    25369, // Cursed Scavenger
                    25746, // Magikus
                    // 80
                    25909, // Carcs
                    25910, // Drak
                    25911, // Ourick
                    25057, // Beacon of Blue Sky
                    25398, // Gaze of Abyss
                    25159, // Paniel the Unicorn
                    25757, // Grousse
                    25441, // Cyrion
                    25163, // Roaring Skylancer
                    25255, // Tiphon
                    25176, // Black Lily
                    // 85
                    25925, // Jisra
                    25926, // Kuka
            };

    private buffOnBossKill()
    {
        addAttackId(RAID_BOSSES);
        addKillId(RAID_BOSSES);
    }
    private final static int min_damage = 0;
    private SkillHolder buff = new SkillHolder(55297, 1);
    private static final Map<Npc, Map<Integer, Integer>> RAIDBOSS_HITS = new ConcurrentHashMap<>(new ConcurrentHashMap<>());

    @Override
    public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
    {
        if (RAIDBOSS_HITS.getOrDefault(npc, null) == null)
        {
            RAIDBOSS_HITS.put(npc, new ConcurrentHashMap<>());
        }
        RAIDBOSS_HITS.get(npc).replace(attacker.getObjectId(), RAIDBOSS_HITS.get(npc).getOrDefault(attacker.getObjectId(), 0) + damage);
        return super.onAttack(npc, attacker, damage, isSummon, skill);
    }

    @Override
    public String onKill(Npc npc, Player killer, boolean isSummon)
    {
        for (Player killers : World.getInstance().getVisibleObjects(npc, Player.class))
        {
            if (RAIDBOSS_HITS.containsKey(npc) && min_damage >= RAIDBOSS_HITS.get(npc).getOrDefault(killers.getObjectId(), 0) && npc.getTemplate().getLevel() > killers.getLevel() - 15 && npc.getTemplate().getLevel() < killers.getLevel() + 15)
            {
                killers.doCast(buff.getSkill());
            }
        }
        RAIDBOSS_HITS.get(npc).clear();
        return super.onKill(npc, killer, isSummon);
    }

    public static void main(String[] args)
    {
        new buffOnBossKill();
    }
}