import type { AxiosRequestConfig } from 'axios';
import api from './axios';
import { buildParams } from '../lib/buildParams';
import type { PageResult } from '../types/api';
import type {
  Execution,
  ExecutionCreateRequest,
  ExecutionListParams,
} from '../types/execution';

export const executionsApi = {
  createExecution: async (
    body: ExecutionCreateRequest
  ): Promise<Execution> => {
    const { data } = await api.post<Execution>('/executions', body);
    return data;
  },

  getExecution: async (
    id: string,
    config?: AxiosRequestConfig
  ): Promise<Execution> => {
    const { data } = await api.get<Execution>(`/executions/${id}`, config);
    return data;
  },

  listExecutions: async (
    params: ExecutionListParams
  ): Promise<PageResult<Execution>> => {
    const { data } = await api.get<PageResult<Execution>>('/executions', {
      params: buildParams({ ...params }),
    });
    return data;
  },
};
