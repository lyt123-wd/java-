import http from '@/utils/http'
import { PageRes } from '@/api/types'

export interface RepairOrderInterface {
  id: number
  userId: number
  roomId?: number
  apartmentId?: number
  type: string
  title: string
  description?: string
  images?: string
  status: number
  handlerId?: number
  handleRemark?: string
  handleTime?: string
  createTime?: string
  updateTime?: string
}

export interface RepairOrderPageQuery {
  pageNum: number
  pageSize: number
  status?: number
  type?: string
  keyword?: string
}

/**
 * 分页查询报修工单
 */
export function getRepairOrderPage(params: RepairOrderPageQuery) {
  return http.get<PageRes<RepairOrderInterface>>('/admin/repair/page', {
    current: params.pageNum,
    size: params.pageSize,
    status: params.status,
    type: params.type,
    keyword: params.keyword,
  })
}

/**
 * 处理报修工单
 */
export function handleRepairOrder(
  id: number | string,
  data: { status: number; handleRemark: string },
) {
  return http.put(`/admin/repair/${id}/handle`, data)
}
