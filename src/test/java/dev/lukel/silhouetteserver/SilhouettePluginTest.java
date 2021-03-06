package dev.lukel.silhouetteserver;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

@PrepareForTest(SilhouettePlugin.class)
@RunWith(PowerMockRunner.class)
public class SilhouettePluginTest {

    @Mock
    SyncTask syncTaskMock;
    @Mock
    Server serverMock;
    @Mock
    PluginManager pluginManagerMock;
    @Mock
    ProtocolListener protocolListenerMock;
    @Mock
    Logger loggerMock;

    SilhouettePlugin plugin;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.plugin = Whitebox.newInstance(SilhouettePlugin.class);
    }

    @Test
    public void onEnable_enablesPlugin() {
        plugin = spy(plugin);
        when(plugin.getLogger()).thenReturn(loggerMock);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        doReturn(syncTaskMock).when(plugin).createSyncTask();
        doReturn(protocolListenerMock).when(plugin).createProtocolListener();
        plugin.onEnable();
        verify(pluginManagerMock, times(1)).registerEvents(any(LoginListener.class), eq(plugin));
        verify(syncTaskMock, times(1)).runTaskTimer(eq(plugin), anyLong(), anyLong());
    }

    @Test
    public void createProtocolLibraryAccessor_createsAccessor() throws Exception {
        protocolListenerMock = createMock(ProtocolListener.class);
        expectNew(ProtocolListener.class, plugin).andReturn(protocolListenerMock);
        replay(protocolListenerMock, ProtocolListener.class);
        ProtocolListener result = plugin.createProtocolListener();
        verify(protocolListenerMock, ProtocolListener.class);
        assertEquals(result, protocolListenerMock);
    }

//    @Test
//    public void createSyncTask_createsTask() throws Exception {
//        syncTaskMock = createMock(SyncTask.class);
//        BukkitCraftPlayerFactory factoryMock = createMock(BukkitCraftPlayerFactory.class);
//        expectNew(BukkitCraftPlayerFactory.class).andReturn(factoryMock);
//        expectNew(SyncTask.class, plugin, factoryMock).andReturn(syncTaskMock);
//
//        replay(factoryMock, BukkitCraftPlayerFactory.class);
//        replay(syncTaskMock, SyncTask.class);
//
//        SyncTask result = plugin.createSyncTask();
//        verify(factoryMock, BukkitCraftPlayerFactory.class);
//        verify(syncTaskMock, SyncTask.class);
//        assertEquals(result, syncTaskMock);
//    }

    @Test
    public void onDisable_disablesPluginCancelsTask() {
        plugin = spy(plugin);
        Whitebox.setInternalState(plugin, "syncTask", syncTaskMock);
        when(plugin.getLogger()).thenReturn(loggerMock);
        plugin.onDisable();
        verify(syncTaskMock, times(1)).cancel();
    }

}