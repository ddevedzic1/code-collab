import {
  Box,
  Tab,
  TabList,
  TabPanel,
  TabPanels,
  Tabs,
} from '@chakra-ui/react';
import { OutputPanel } from './OutputPanel';
import { HistoryPanel } from './HistoryPanel';
import type { Execution } from '../../../types/execution';

interface RightPanelTabsProps {
  snippetId: string;
  execution: Execution | null;
  historyRefreshToken: number;
  tabIndex: number;
  onTabChange: (index: number) => void;
  onSelectExecution: (execution: Execution) => void;
}

export const RightPanelTabs = ({
  snippetId,
  execution,
  historyRefreshToken,
  tabIndex,
  onTabChange,
  onSelectExecution,
}: RightPanelTabsProps) => (
  <Tabs
    index={tabIndex}
    onChange={onTabChange}
    display="flex"
    flexDirection="column"
    h="100%"
    isLazy
    colorScheme="blue"
  >
    <TabList flexShrink={0} px={2}>
      <Tab>Output</Tab>
      <Tab>History</Tab>
    </TabList>

    <TabPanels flex="1" overflow="hidden">
      <TabPanel p={0} h="100%">
        <Box h="100%">
          <OutputPanel execution={execution} />
        </Box>
      </TabPanel>
      <TabPanel p={0} h="100%">
        <Box h="100%">
          <HistoryPanel
            snippetId={snippetId}
            refreshToken={historyRefreshToken}
            activeExecutionId={execution?.id}
            onSelect={onSelectExecution}
          />
        </Box>
      </TabPanel>
    </TabPanels>
  </Tabs>
);
