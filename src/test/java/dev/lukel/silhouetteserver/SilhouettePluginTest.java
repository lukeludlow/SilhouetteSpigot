package dev.lukel.silhouetteserver;

import com.comphenix.protocol.ProtocolConfig;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import net.minecraft.SharedConstants;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

//@PrepareForTest(SilhouettePlugin.class)
@PrepareForTest({SilhouettePlugin.class, ProtocolLibrary.class, ProtocolLibraryAccessor.class, BasicErrorReporter.class})
//@PrepareForTest({SilhouettePlugin.class, ProtocolLibraryAccessor.class})
@RunWith(PowerMockRunner.class)
public class SilhouettePluginTest {

    @Mock
    SyncTask syncTaskMock;
    @Mock
    Server serverMock;
    @Mock
    PluginManager pluginManagerMock;
    @Mock
    ProtocolManager protocolManagerMock;
//    @Mock
//    ProtocolLibraryAccessor protocolLibraryMock;

    SilhouettePlugin plugin;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.plugin = Whitebox.newInstance(SilhouettePlugin.class);
    }

    @Test
    public void onEnable_enablesPlugin() throws Exception {
        plugin = spy(plugin);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        doReturn(syncTaskMock).when(plugin).createSyncTask();

        SharedConstants.a();
        String serverVersion = CraftServer.class.getPackage().getImplementationVersion();
        String releaseTarget = SharedConstants.b().getReleaseTarget();
        when(serverMock.getVersion()).thenReturn(serverVersion + " (MC: " + releaseTarget + ")");

        doNothing().when(protocolManagerMock).addPacketListener(any(PacketListener.class));
        PowerMockito.mockStatic(ProtocolLibraryAccessor.class);
        when(ProtocolLibraryAccessor.getProtocolManager()).thenAnswer((Answer<ProtocolManager>) invocation -> protocolManagerMock);
//        PowerMockito.when(ProtocolLibraryAccessor.getProtocolManager()).thenReturn(protocolManagerMock);

//        doReturn(protocolManagerMock).when(ProtocolLibraryAccessor.getProtocolManager());
//        when(ProtocolLibrary.getProtocolManager()).thenReturn(protocolManagerMock);
//        ProtocolLibraryAccessor protocolLibraryMock = createMock(ProtocolLibraryAccessor.class);
//        expectNew(ProtocolLibraryAccessor.class).andReturn(protocolLibraryMock);
//        replay(protocolLibraryMock, ProtocolLibraryAccessor.class);
//        when(protocolLibraryMock.getProtocolManager()).thenReturn(protocolManagerMock);

        plugin.onEnable();
        verify(pluginManagerMock, times(1)).registerEvents(any(LoginListener.class), eq(plugin));
        verify(syncTaskMock, times(1)).runTaskTimer(eq(plugin), anyLong(), anyLong());
    }

    @Test
    public void onEnable_withException_disablesPlugin() throws UnsupportedMinecraftVersionException {
        plugin = spy(plugin);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        doThrow(UnsupportedMinecraftVersionException.class).when(plugin).createSyncTask();
        plugin.onEnable();
        verify(pluginManagerMock, times(1)).disablePlugin(plugin);
    }

    @Test
    public void createSyncTask_createsTask() throws Exception {
        syncTaskMock = createMock(SyncTask.class);
        BukkitCraftPlayerFactory factoryMock = createMock(BukkitCraftPlayerFactory.class);
        expectNew(BukkitCraftPlayerFactory.class).andReturn(factoryMock);
        expectNew(SyncTask.class, plugin, factoryMock).andReturn(syncTaskMock);

        replay(factoryMock, BukkitCraftPlayerFactory.class);
        replay(syncTaskMock, SyncTask.class);

        SyncTask result = plugin.createSyncTask();
        verify(factoryMock, BukkitCraftPlayerFactory.class);
        verify(syncTaskMock, SyncTask.class);
        assertEquals(result, syncTaskMock);
    }

    @Test
    public void onDisable_disablesPluginCancelsTask() {
        Whitebox.setInternalState(plugin, "syncTask", syncTaskMock);
        plugin.onDisable();
        verify(syncTaskMock, times(1)).cancel();
    }

}