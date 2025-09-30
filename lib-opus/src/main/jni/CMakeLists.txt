cmake_minimum_required(VERSION 3.13)

project(opus-lib)

set(CMAKE_CXX_STANDARD 17)
set(OPUS_DIR ${CMAKE_SOURCE_DIR}/../opus)

# 设置编译标志
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2")  # 优化级别
# 16k对齐编译选项
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-z,max-page-size=16384")
add_definitions(
        -DOPUS_BUILD
        -DHAVE_CONFIG_H
        -DVAR_ARRAYS
        -DUSE_ALLOCA     # 添加这个定义
        -DCUSTOM_MODES   # 添加这个定义
        -DOVERRIDE_OPUS_INTERNAL # 添加这个定义
)

include_directories(
        ${CMAKE_SOURCE_DIR}
        ${OPUS_DIR}/include
        ${OPUS_DIR}/src
        ${OPUS_DIR}/celt
        ${OPUS_DIR}/silk
        ${OPUS_DIR}/silk/float
        ${OPUS_DIR}/silk/arm  # 添加ARM特定代码
)

add_library(opus-lib SHARED
    ${CMAKE_SOURCE_DIR}/opus_jni.cpp
    # src文件
    ${OPUS_DIR}/src/opus.c
    ${OPUS_DIR}/src/opus_decoder.c
    ${OPUS_DIR}/src/opus_multistream.c
    ${OPUS_DIR}/src/opus_multistream_decoder.c
    ${OPUS_DIR}/src/mapping_matrix.c

    # celt源文件
    ${OPUS_DIR}/celt/bands.c
    ${OPUS_DIR}/celt/celt.c
    ${OPUS_DIR}/celt/celt_decoder.c
    ${OPUS_DIR}/celt/celt_lpc.c
    ${OPUS_DIR}/celt/entdec.c
    ${OPUS_DIR}/celt/entcode.c
    ${OPUS_DIR}/celt/entenc.c
    ${OPUS_DIR}/celt/kiss_fft.c
    ${OPUS_DIR}/celt/laplace.c
    ${OPUS_DIR}/celt/mathops.c
    ${OPUS_DIR}/celt/mdct.c
    ${OPUS_DIR}/celt/modes.c
    ${OPUS_DIR}/celt/pitch.c
    ${OPUS_DIR}/celt/quant_bands.c
    ${OPUS_DIR}/celt/rate.c
    ${OPUS_DIR}/celt/vq.c
    # celt 额外源文件
    ${OPUS_DIR}/celt/entenc.c
    ${OPUS_DIR}/celt/laplace.c
    ${OPUS_DIR}/celt/cwrs.c

    # silk源文件
    ${OPUS_DIR}/silk/CNG.c
    ${OPUS_DIR}/silk/code_signs.c
    ${OPUS_DIR}/silk/dec_API.c
    ${OPUS_DIR}/silk/decode_core.c
    ${OPUS_DIR}/silk/decode_frame.c
    ${OPUS_DIR}/silk/decode_indices.c
    ${OPUS_DIR}/silk/decode_parameters.c
    ${OPUS_DIR}/silk/decode_pitch.c
    ${OPUS_DIR}/silk/decode_pulses.c
    ${OPUS_DIR}/silk/decoder_set_fs.c
    ${OPUS_DIR}/silk/init_decoder.c
    ${OPUS_DIR}/silk/LPC_analysis_filter.c
    ${OPUS_DIR}/silk/LPC_inv_pred_gain.c
    ${OPUS_DIR}/silk/NLSF2A.c
    ${OPUS_DIR}/silk/NLSF_decode.c
    ${OPUS_DIR}/silk/PLC.c
    ${OPUS_DIR}/silk/resampler.c
    ${OPUS_DIR}/silk/resampler_down2.c
    ${OPUS_DIR}/silk/resampler_private_AR2.c
    ${OPUS_DIR}/silk/resampler_private_down_FIR.c
    ${OPUS_DIR}/silk/resampler_private_IIR_FIR.c
    ${OPUS_DIR}/silk/resampler_private_up2_HQ.c
    ${OPUS_DIR}/silk/resampler_rom.c
    ${OPUS_DIR}/silk/shell_coder.c
    ${OPUS_DIR}/silk/tables_gain.c
    ${OPUS_DIR}/silk/tables_LTP.c
    ${OPUS_DIR}/silk/tables_NLSF_CB_NB_MB.c
    ${OPUS_DIR}/silk/tables_NLSF_CB_WB.c
    ${OPUS_DIR}/silk/tables_other.c
    ${OPUS_DIR}/silk/tables_pitch_lag.c
    ${OPUS_DIR}/silk/tables_pulses_per_block.c
    ${OPUS_DIR}/silk/bwexpander.c
    ${OPUS_DIR}/silk/gain_quant.c
    ${OPUS_DIR}/silk/stereo_MS_to_LR.c
    ${OPUS_DIR}/silk/stereo_decode_pred.c
    ${OPUS_DIR}/silk/LPC_fit.c
    ${OPUS_DIR}/silk/bwexpander_32.c
    ${OPUS_DIR}/silk/lin2log.c
    ${OPUS_DIR}/silk/log2lin.c
    ${OPUS_DIR}/silk/sum_sqr_shift.c
    ${OPUS_DIR}/silk/NLSF_unpack.c
    ${OPUS_DIR}/silk/NLSF_stabilize.c
    ${OPUS_DIR}/silk/table_LSF_cos.c
    # silk 额外源文件
    ${OPUS_DIR}/silk/sort.c
    ${OPUS_DIR}/silk/pitch_est_tables.c
    ${OPUS_DIR}/silk/tables_pulses_per_block.c
    ${OPUS_DIR}/silk/control_SNR.c
    ${OPUS_DIR}/silk/tables_pitch_lag.c
)

find_library(log-lib log)
target_link_libraries(
        opus-lib
        ${log-lib}
)
