package dev.lukel.silhouetteserver;

import dev.lukel.silhouetteserver.player.BukkitCraftPlayer;
import dev.lukel.silhouetteserver.player.BukkitCraftPlayerFactory;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class SyncTaskTest {

    @Mock
    Server serverMock;
    @Mock
    BukkitCraftPlayer craftPlayerMock;
    @Mock
    SilhouettePlugin pluginMock;
    @Mock
    BukkitCraftPlayerFactory factoryMock;

    SyncTask syncTask;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void syncTaskCreatesBukkitPlayer() {
        new SyncTask(pluginMock, factoryMock);
        verify(factoryMock, times(1)).getBukkitCraftPlayer(serverMock);
    }

    @Test
    public void run_shouldUpdatePlayerWithOtherPlayersInfo() {
        Player playerOneMock = createMock(Player.class);
        Player playerTwoMock = createMock(Player.class);
        Player playerThreeMock = createMock(Player.class);
        when(playerOneMock.getEntityId()).thenReturn(1);
        when(playerTwoMock.getEntityId()).thenReturn(2);
        when(playerThreeMock.getEntityId()).thenReturn(3);
        List<Player> onlinePlayers = Arrays.asList(playerOneMock, playerTwoMock, playerThreeMock);
        doReturn(onlinePlayers).when(serverMock).getOnlinePlayers();

        SyncTask syncTask = new SyncTask(pluginMock, factoryMock);
    }

}