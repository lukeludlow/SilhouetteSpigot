package dev.lukel.silhouetteserver;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SyncTask.class)
public class SyncTaskTest {

    @Mock
    Server serverMock;
    @Mock
    SilhouettePlugin pluginMock;
    @Mock
    ProtocolListener protocolAccessorMock;
    @Mock
    Logger loggerMock;
    @Mock
    PacketBuilder packetBuilderMock;
    @Mock
    ProtocolSender protocolSenderMock;

    @Mock
    Player playerOneMock;
    @Mock
    Player playerTwoMock;
    @Mock
    Player playerThreeMock;
    @Mock
    Location locationMock;

//    SyncTask syncTask;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(pluginMock.getServer()).thenReturn(serverMock);
        when(pluginMock.getLogger()).thenReturn(loggerMock);
        when(playerOneMock.getEntityId()).thenReturn(1);
        when(playerTwoMock.getEntityId()).thenReturn(2);
        when(playerThreeMock.getEntityId()).thenReturn(3);
        when(playerOneMock.getLocation()).thenReturn(locationMock);
        when(playerTwoMock.getLocation()).thenReturn(locationMock);
        when(playerThreeMock.getLocation()).thenReturn(locationMock);
        List<Player> onlinePlayers = Arrays.asList(playerOneMock, playerTwoMock, playerThreeMock);
        doReturn(onlinePlayers).when(serverMock).getOnlinePlayers();
        when(serverMock.getViewDistance()).thenReturn(10);  // 10 chunks
        when(pluginMock.getServer()).thenReturn(serverMock);
    }

    @Test
    public void run_shouldUpdatePlayerWithOtherPlayersInfo() {
        final double distance = 200;
        when(locationMock.distance(any(Location.class))).thenReturn(distance);

        SyncTask syncTask = spy(new SyncTask(pluginMock, protocolAccessorMock, packetBuilderMock, protocolSenderMock));
        syncTask.run();

        verify(syncTask, times(2)).updatePlayer(eq(playerOneMock), any(Player.class));
        verify(syncTask, times(2)).updatePlayer(eq(playerTwoMock), any(Player.class));
        verify(syncTask, times(2)).updatePlayer(eq(playerThreeMock), any(Player.class));
    }

    @Test
    public void run_playersWithinRenderDistance_shouldNotSendUpdates() {
        final double distance = 10;
        when(locationMock.distance(any(Location.class))).thenReturn(distance);

        SyncTask syncTask = spy(new SyncTask(pluginMock, protocolAccessorMock, packetBuilderMock, protocolSenderMock));
        syncTask.run();

        verify(syncTask, never()).updatePlayer(eq(playerOneMock), any(Player.class));
        verify(syncTask, never()).updatePlayer(eq(playerTwoMock), any(Player.class));
        verify(syncTask, never()).updatePlayer(eq(playerThreeMock), any(Player.class));
    }

    @Test
    public void onPlayerJoin_shouldSendPlayerInfoSpawnPacketsToAllOtherPlayers() {
    }

}